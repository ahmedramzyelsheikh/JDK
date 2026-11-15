import java.util.List;

/**
 * Abstract classes (template method), interfaces with default/static/private methods,
 * the default-method diamond, and functional interfaces.
 * Run: java AbstractionInterfaces.java
 */
public class AbstractionInterfaces {

    // ---------- abstract class: shared state + template method ----------
    abstract static class Shape {
        private final String name;

        protected Shape(String name) {               // abstract classes CAN have constructors
            this.name = name;
        }

        abstract double area();                      // subclasses must implement

        final String describe() {                    // template method: fixed algorithm
            return "%s with area %.2f".formatted(name, area());
        }
    }

    static class Circle extends Shape {
        private final double r;
        Circle(double r) { super("Circle"); this.r = r; }
        @Override double area() { return Math.PI * r * r; }
    }

    static class Rectangle extends Shape {
        private final double w, h;
        Rectangle(double w, double h) { super("Rectangle"); this.w = w; this.h = h; }
        @Override double area() { return w * h; }
    }

    // ---------- interface features ----------
    interface Payable {
        double TAX = 0.14;                           // implicitly public static final

        double amount();                             // implicitly public abstract

        default double amountWithTax() {             // Java 8 default method
            return round(amount() * (1 + TAX));
        }

        static Payable of(double value) {            // Java 8 static method (NOT inherited)
            return () -> value;                      // a lambda, because Payable is functional
        }

        private double round(double v) {            // Java 9 private helper
            return Math.round(v * 100) / 100.0;
        }
    }

    // ---------- diamond with default methods ----------
    interface Camera {
        default String start() { return "camera on"; }
    }

    interface Phone {
        default String start() { return "phone on"; }
    }

    static class SmartPhone implements Camera, Phone {
        @Override
        public String start() {                      // MUST override: both interfaces define start()
            return Camera.super.start() + " + " + Phone.super.start();
        }
    }

    // rule 1: class wins over interface
    static class Device {
        public String start() { return "device start (class wins)"; }
    }

    static class Tablet extends Device implements Camera { }

    // rule 2: more specific interface wins
    interface ProCamera extends Camera {
        @Override default String start() { return "pro camera on (more specific wins)"; }
    }

    static class Dslr implements Camera, ProCamera { }

    public static void main(String[] args) {
        System.out.println("=== Abstract class + template method ===");
        List<Shape> shapes = List.of(new Circle(1), new Rectangle(2, 3));
        shapes.forEach(s -> System.out.println("  " + s.describe()));
        // new Shape("x");                           // compile error: Shape is abstract

        System.out.println("\n=== Interface default / static / private ===");
        Payable invoice = Payable.of(100);
        System.out.println("  amount " + invoice.amount() + ", with tax " + invoice.amountWithTax() + ", TAX constant " + Payable.TAX);
        Payable anonymous = new Payable() {          // anonymous class implementing the interface
            @Override public double amount() { return 50; }
        };
        System.out.println("  anonymous class with tax: " + anonymous.amountWithTax());

        System.out.println("\n=== Default-method conflicts ===");
        System.out.println("  SmartPhone: " + new SmartPhone().start());
        System.out.println("  Tablet:     " + new Tablet().start());
        System.out.println("  Dslr:       " + new Dslr().start());
    }
}
