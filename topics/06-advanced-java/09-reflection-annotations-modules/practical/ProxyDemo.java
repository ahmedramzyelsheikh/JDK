import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK dynamic proxies: a home-made @Transactional and @Timed, and why self-invocation bypasses the proxy
 * (the same reason Spring's @Transactional is ignored on this.someMethod() calls).
 * Run: java ProxyDemo.java
 */
public class ProxyDemo {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Transactional { }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Timed { }

    interface OrderService {
        @Transactional
        void placeOrder(String item);

        @Transactional
        void placeTwoOrders(String a, String b);

        @Timed
        String report();
    }

    static class RealOrderService implements OrderService {
        @Override
        public void placeOrder(String item) {
            if (item.isBlank()) {
                throw new IllegalArgumentException("empty item");
            }
            System.out.println("      INSERT order " + item);
        }

        @Override
        public void placeTwoOrders(String a, String b) {
            placeOrder(a);              // self-invocation: this.placeOrder -> NOT through the proxy
            placeOrder(b);
        }

        @Override
        public String report() {
            return "2 orders";
        }
    }

    /** The interceptor: runs before/after every call on the proxy. */
    static class TransactionHandler implements InvocationHandler {
        private final Object target;

        TransactionHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method impl = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            boolean tx = method.isAnnotationPresent(Transactional.class);
            boolean timed = method.isAnnotationPresent(Timed.class);
            long start = System.nanoTime();
            if (tx) System.out.println("    [proxy] BEGIN for " + method.getName());
            try {
                Object result = impl.invoke(target, args);
                if (tx) System.out.println("    [proxy] COMMIT");
                return result;
            } catch (InvocationTargetException e) {
                if (tx) System.out.println("    [proxy] ROLLBACK because " + e.getCause().getMessage());
                throw e.getCause();
            } finally {
                if (timed) System.out.printf("    [proxy] %s took %d us%n", method.getName(), (System.nanoTime() - start) / 1000);
            }
        }
    }

    public static void main(String[] args) {
        OrderService service = (OrderService) Proxy.newProxyInstance(
                OrderService.class.getClassLoader(),
                new Class<?>[] {OrderService.class},
                new TransactionHandler(new RealOrderService()));

        System.out.println("proxy class: " + service.getClass().getName() + " implements OrderService? " + (service instanceof OrderService));

        System.out.println("\n1) normal call through the proxy:");
        service.placeOrder("book");

        System.out.println("\n2) failing call -> rollback:");
        try {
            service.placeOrder(" ");
        } catch (IllegalArgumentException e) {
            System.out.println("    caller got: " + e.getMessage());
        }

        System.out.println("\n3) @Timed:");
        System.out.println("    result: " + service.report());

        System.out.println("\n4) self-invocation: only ONE transaction, inner placeOrder calls are not intercepted:");
        service.placeTwoOrders("pen", "cup");
    }
}
