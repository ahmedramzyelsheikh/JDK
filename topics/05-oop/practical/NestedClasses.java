import java.util.Iterator;
import java.util.function.IntSupplier;

/**
 * Static nested, inner, local, anonymous classes and lambdas; captured variables and `this`.
 * Run: java NestedClasses.java
 */
public class NestedClasses {

    private final String owner = "outer object";

    /** Static nested: no reference to an outer instance. Often used for builders. */
    static class Pizza {
        private final String size;
        private final boolean cheese;

        private Pizza(Builder b) { size = b.size; cheese = b.cheese; }

        static class Builder {
            private String size = "M";
            private boolean cheese;
            Builder size(String s) { size = s; return this; }
            Builder cheese() { cheese = true; return this; }
            Pizza build() { return new Pizza(this); }
        }

        @Override public String toString() { return "Pizza(" + size + (cheese ? ", cheese" : "") + ")"; }
    }

    /** Inner class: each instance is tied to an outer instance (Outer.this). */
    class Range implements Iterable<Integer> {
        private final int from, to;

        Range(int from, int to) { this.from = from; this.to = to; }

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {                            // anonymous class
                int next = from;
                @Override public boolean hasNext() { return next <= to; }
                @Override public Integer next() { return next++; }
            };
        }

        String whoOwnsMe() { return NestedClasses.this.owner; }
    }

    void thisInLambdaVsAnonymous() {
        Runnable lambda = () -> System.out.println("  lambda this    = " + this.getClass().getSimpleName());
        Runnable anon = new Runnable() {
            @Override public void run() {
                System.out.println("  anonymous this = " + this.getClass().getName() + " (the anonymous object)");
            }
        };
        lambda.run();
        anon.run();
    }

    public static void main(String[] args) {
        System.out.println("=== Static nested (builder) ===");
        System.out.println("  " + new Pizza.Builder().size("L").cheese().build());

        System.out.println("\n=== Inner class needs an outer instance ===");
        NestedClasses outer = new NestedClasses();
        NestedClasses.Range range = outer.new Range(1, 5);      // outer.new Inner()
        StringBuilder sb = new StringBuilder();
        for (int i : range) {
            sb.append(i).append(' ');
        }
        System.out.println("  range: " + sb + "| owner: " + range.whoOwnsMe());

        System.out.println("\n=== Local class + effectively final capture ===");
        int base = 10;                                          // effectively final (never reassigned)
        class Adder {                                           // local class
            int add(int x) { return base + x; }
        }
        System.out.println("  local Adder.add(5) = " + new Adder().add(5));
        IntSupplier supplier = () -> base * 2;                  // lambda captures base
        System.out.println("  lambda capture = " + supplier.getAsInt());
        // base++;                                              // would make both captures a compile error

        System.out.println("\n=== this in lambda vs anonymous class ===");
        outer.thisInLambdaVsAnonymous();
    }
}
