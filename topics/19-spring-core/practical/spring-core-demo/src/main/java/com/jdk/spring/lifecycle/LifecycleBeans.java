package com.jdk.spring.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Scopes, lifecycle callbacks and a custom BeanPostProcessor. */
public final class LifecycleBeans {

    private LifecycleBeans() {
    }

    /** Global event log so tests can see the order in which Spring called things. */
    public static final List<String> EVENTS = new ArrayList<>();

    @Component
    public static class CacheWarmer implements InitializingBean {
        private boolean warm;

        public CacheWarmer() {
            EVENTS.add("1 constructor");
        }

        @PostConstruct
        void postConstruct() {
            EVENTS.add("3 @PostConstruct");
            warm = true;
        }

        @Override
        public void afterPropertiesSet() {
            EVENTS.add("4 afterPropertiesSet");
        }

        @PreDestroy
        void preDestroy() {
            EVENTS.add("6 @PreDestroy");
        }

        public boolean isWarm() {
            return warm;
        }
    }

    /** Spring's own extension point: runs for EVERY bean (this is how @Autowired, AOP, @Async... are implemented). */
    @Component
    public static class LoggingPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof CacheWarmer) {
                EVENTS.add("2 BeanPostProcessor before init");
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof CacheWarmer) {
                EVENTS.add("5 BeanPostProcessor after init (proxies are created here)");
            }
            return bean;
        }
    }

    @Component
    @Scope("prototype")                      // a NEW instance every time it is requested
    public static class ShoppingCart {
        private final String id = UUID.randomUUID().toString().substring(0, 8);
        private final List<String> items = new ArrayList<>();

        public String id() { return id; }
        public List<String> items() { return items; }
    }

    /** Singleton holding a prototype directly: the trap. */
    @Component
    public static class BrokenCheckout {
        private final ShoppingCart cart;                     // injected ONCE -> always the same cart

        public BrokenCheckout(ShoppingCart cart) {
            this.cart = cart;
        }

        public ShoppingCart newCartForCustomer() {
            return cart;
        }
    }

    /** Singleton asking the container for a fresh prototype each time: the fix. */
    @Component
    public static class FixedCheckout {
        private final ObjectProvider<ShoppingCart> carts;

        public FixedCheckout(ObjectProvider<ShoppingCart> carts) {
            this.carts = carts;
        }

        public ShoppingCart newCartForCustomer() {
            return carts.getObject();
        }
    }
}
