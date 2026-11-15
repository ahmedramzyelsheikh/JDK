import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * The five SOLID principles, each as a small "after" design that runs.
 * Run: java SolidPrinciples.java
 */
public class SolidPrinciples {

    record Item(String name, BigDecimal price, int qty) {
        BigDecimal total() { return price.multiply(BigDecimal.valueOf(qty)); }
    }

    record Order(String customerEmail, boolean loyalCustomer, List<Item> items) { }

    // ---------- S: one reason to change per class ----------
    static class OrderCalculator {                      // business rule
        BigDecimal subtotal(Order o) {
            return o.items().stream().map(Item::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    static class ReceiptFormatter {                     // presentation
        String format(Order o, BigDecimal total) {
            return "Receipt for " + o.customerEmail() + ": " + o.items().size() + " items, total " + total;
        }
    }

    // ---------- O: add discounts by adding classes, not editing Checkout ----------
    interface DiscountPolicy {
        BigDecimal apply(Order order, BigDecimal subtotal);
    }

    static class NoDiscount implements DiscountPolicy {
        public BigDecimal apply(Order o, BigDecimal s) { return s; }
    }

    static class LoyaltyDiscount implements DiscountPolicy {
        public BigDecimal apply(Order o, BigDecimal s) {
            return o.loyalCustomer() ? s.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP) : s;
        }
    }

    static class BulkDiscount implements DiscountPolicy {     // NEW rule: zero changes elsewhere
        public BigDecimal apply(Order o, BigDecimal s) {
            int units = o.items().stream().mapToInt(Item::qty).sum();
            return units >= 10 ? s.subtract(new BigDecimal("20")) : s;
        }
    }

    // ---------- I: small role interfaces ----------
    interface Notifier {
        void notify(String to, String message);
    }

    interface Archiver {
        void archive(String document);
    }

    static class EmailNotifier implements Notifier {           // needs only Notifier, not archiving
        final List<String> sent = new ArrayList<>();
        public void notify(String to, String message) { sent.add("email to " + to + ": " + message); }
    }

    // ---------- D: high-level Checkout depends on abstractions, injected via constructor ----------
    static class Checkout {
        private final OrderCalculator calculator;
        private final List<DiscountPolicy> discounts;
        private final ReceiptFormatter formatter;
        private final Notifier notifier;

        Checkout(OrderCalculator calculator, List<DiscountPolicy> discounts, ReceiptFormatter formatter, Notifier notifier) {
            this.calculator = calculator;
            this.discounts = discounts;
            this.formatter = formatter;
            this.notifier = notifier;
        }

        BigDecimal checkout(Order order) {
            BigDecimal total = calculator.subtotal(order);
            for (DiscountPolicy d : discounts) {
                total = d.apply(order, total);
            }
            notifier.notify(order.customerEmail(), formatter.format(order, total));
            return total;
        }
    }

    // ---------- L: a subtype that breaks the base contract ----------
    static class Rectangle {
        protected int width, height;
        void setWidth(int w) { width = w; }
        void setHeight(int h) { height = h; }
        int area() { return width * height; }
    }

    static class Square extends Rectangle {               // VIOLATES LSP
        @Override void setWidth(int w) { width = w; height = w; }
        @Override void setHeight(int h) { width = h; height = h; }
    }

    static int stretch(Rectangle r) {                       // client written against Rectangle's contract
        r.setWidth(5);
        r.setHeight(4);
        return r.area();                                    // expects 20
    }

    sealed interface Shape permits Rect, Sq { int area(); }  // fix: no false "is-a"
    record Rect(int w, int h) implements Shape { public int area() { return w * h; } }
    record Sq(int side) implements Shape { public int area() { return side * side; } }

    public static void main(String[] args) {
        EmailNotifier notifier = new EmailNotifier();
        Checkout checkout = new Checkout(new OrderCalculator(),
                List.of(new LoyaltyDiscount(), new BulkDiscount()),       // composition decided by the caller
                new ReceiptFormatter(), notifier);

        Order order = new Order("ali@x.com", true, List.of(
                new Item("pen", new BigDecimal("2.00"), 10),
                new Item("notebook", new BigDecimal("15.00"), 2)));
        System.out.println("S/O/D: total = " + checkout.checkout(order));
        System.out.println("I:     " + notifier.sent);

        System.out.println("L:     stretch(Rectangle) = " + stretch(new Rectangle())
                + ", stretch(Square) = " + stretch(new Square()) + "  <- surprise! Square is not a valid Rectangle");
        System.out.println("L fix: " + List.of(new Rect(5, 4), new Sq(4)).stream().map(s -> s + " area " + s.area()).toList());
    }
}
