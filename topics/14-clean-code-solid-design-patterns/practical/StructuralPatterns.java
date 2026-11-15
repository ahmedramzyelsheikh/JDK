import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter, Decorator, Facade, Proxy and Composite.
 * Run: java StructuralPatterns.java
 */
public class StructuralPatterns {

    // ---------- Adapter: make a legacy/third-party API fit our interface ----------
    interface PaymentProcessor { String pay(String customer, double amount); }

    static class LegacyBankApi {                                  // can't change it
        int sendMoney(String accountNo, long cents) { return 200; }
    }

    static class LegacyBankAdapter implements PaymentProcessor {
        private final LegacyBankApi bank = new LegacyBankApi();
        public String pay(String customer, double amount) {
            int code = bank.sendMoney("ACC-" + customer, Math.round(amount * 100));   // translate the call
            return code == 200 ? "paid " + amount + " via legacy bank" : "failed";
        }
    }

    // ---------- Decorator: add behaviour by wrapping, same interface ----------
    interface DataStore { void write(String data); String read(); }

    static class MemoryStore implements DataStore {
        private String data = "";
        public void write(String d) { data = d; }
        public String read() { return data; }
    }

    abstract static class StoreDecorator implements DataStore {
        protected final DataStore inner;
        StoreDecorator(DataStore inner) { this.inner = inner; }
    }

    static class Base64Decorator extends StoreDecorator {          // "encryption"
        Base64Decorator(DataStore inner) { super(inner); }
        public void write(String d) { inner.write(Base64.getEncoder().encodeToString(d.getBytes())); }
        public String read() { return new String(Base64.getDecoder().decode(inner.read())); }
    }

    static class LoggingDecorator extends StoreDecorator {
        final List<String> log = new ArrayList<>();
        LoggingDecorator(DataStore inner) { super(inner); }
        public void write(String d) { log.add("write " + d.length() + " chars"); inner.write(d); }
        public String read() { log.add("read"); return inner.read(); }
    }

    // ---------- Facade: one simple entry point to a complex subsystem ----------
    static class Inventory { boolean reserve(String sku) { return !sku.equals("sold-out"); } }
    static class Billing { String charge(String c, double a) { return "INV-" + c.hashCode() % 1000; } }
    static class Shipping { String ship(String sku) { return "TRACK-" + sku.toUpperCase(); } }

    static class OrderFacade {
        private final Inventory inventory = new Inventory();
        private final Billing billing = new Billing();
        private final Shipping shipping = new Shipping();

        String placeOrder(String customer, String sku, double price) {
            if (!inventory.reserve(sku)) {
                return "out of stock: " + sku;
            }
            return "invoice " + billing.charge(customer, price) + ", " + shipping.ship(sku);
        }
    }

    // ---------- Proxy: a caching proxy generated at runtime ----------
    interface PriceService { double priceOf(String sku); }

    static class SlowPriceService implements PriceService {
        int calls;
        public double priceOf(String sku) { calls++; return sku.length() * 10.0; }    // pretend it's a remote call
    }

    @SuppressWarnings("unchecked")
    static <T> T cachingProxy(T target, Class<T> type) {
        Map<String, Object> cache = new HashMap<>();
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
            String key = method.getName() + java.util.Arrays.toString(args);
            if (!cache.containsKey(key)) {
                cache.put(key, method.invoke(target, args));
            }
            return cache.get(key);
        });
    }

    // ---------- Composite: treat leaves and groups uniformly ----------
    sealed interface CatalogNode permits Product, Category { double totalValue(); String render(String indent); }

    record Product(String name, double price) implements CatalogNode {
        public double totalValue() { return price; }
        public String render(String indent) { return indent + "- " + name + " " + price + "\n"; }
    }

    record Category(String name, List<CatalogNode> children) implements CatalogNode {
        public double totalValue() { return children.stream().mapToDouble(CatalogNode::totalValue).sum(); }
        public String render(String indent) {
            StringBuilder sb = new StringBuilder(indent + "+ " + name + " (" + totalValue() + ")\n");
            children.forEach(c -> sb.append(c.render(indent + "  ")));
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        PaymentProcessor processor = new LegacyBankAdapter();
        System.out.println("Adapter:   " + processor.pay("ali", 49.99));

        LoggingDecorator logged = new LoggingDecorator(new Base64Decorator(new MemoryStore()));
        logged.write("secret order data");
        System.out.println("Decorator: read back '" + logged.read() + "', log " + logged.log);

        OrderFacade shop = new OrderFacade();
        System.out.println("Facade:    " + shop.placeOrder("sara", "book-42", 30) + " | " + shop.placeOrder("sara", "sold-out", 5));

        SlowPriceService real = new SlowPriceService();
        PriceService cached = cachingProxy(real, PriceService.class);
        double sum = cached.priceOf("pen") + cached.priceOf("pen") + cached.priceOf("laptop");
        System.out.println("Proxy:     sum " + sum + " with only " + real.calls + " real calls (3 requests)");

        Category catalog = new Category("All", List.of(
                new Category("Tech", List.of(new Product("Mouse", 25), new Product("Monitor", 280))),
                new Product("Gift card", 50)));
        System.out.print("Composite:\n" + catalog.render("  "));
    }
}
