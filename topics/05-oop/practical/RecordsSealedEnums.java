import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Records (validation, shallow immutability), sealed hierarchies with exhaustive switch,
 * enums with fields/behaviour, EnumMap and the enum singleton.
 * Run: java RecordsSealedEnums.java
 */
public class RecordsSealedEnums {

    // ---------- records ----------
    record Money(BigDecimal amount, String currency) {
        Money {                                                   // compact canonical constructor
            Objects.requireNonNull(currency, "currency");
            if (amount.signum() < 0) {
                throw new IllegalArgumentException("amount must be >= 0");
            }
        }

        Money add(Money other) {
            if (!currency.equals(other.currency)) {
                throw new IllegalArgumentException("currency mismatch");
            }
            return new Money(amount.add(other.amount), currency);
        }

        static Money egp(String value) {                          // static factory
            return new Money(new BigDecimal(value), "EGP");
        }
    }

    record Team(String name, List<String> members) {
        Team {
            members = List.copyOf(members);                       // make it truly immutable
        }
    }

    record LeakyTeam(String name, List<String> members) { }       // shallowly immutable only

    // ---------- sealed + records = algebraic data type ----------
    sealed interface Expr permits Num, Add, Mul, Neg { }
    record Num(double value) implements Expr { }
    record Add(Expr left, Expr right) implements Expr { }
    record Mul(Expr left, Expr right) implements Expr { }
    record Neg(Expr inner) implements Expr { }

    static double eval(Expr e) {
        return switch (e) {                                       // exhaustive: no default needed
            case Num n -> n.value();
            case Add(Expr l, Expr r) -> eval(l) + eval(r);        // record deconstruction pattern
            case Mul(Expr l, Expr r) -> eval(l) * eval(r);
            case Neg(Expr inner) -> -eval(inner);
        };
    }

    static String show(Expr e) {
        return switch (e) {
            case Num n -> String.valueOf(n.value());
            case Add a -> "(" + show(a.left()) + " + " + show(a.right()) + ")";
            case Mul m -> show(m.left()) + " * " + show(m.right());
            case Neg n -> "-" + show(n.inner());
        };
    }

    // ---------- enums with state and behaviour ----------
    enum Operation {
        PLUS("+") { double apply(double a, double b) { return a + b; } },
        MINUS("-") { double apply(double a, double b) { return a - b; } },
        TIMES("*") { double apply(double a, double b) { return a * b; } },
        DIVIDE("/") { double apply(double a, double b) { return a / b; } };

        private final String symbol;

        Operation(String symbol) {                                // implicitly private
            this.symbol = symbol;
        }

        abstract double apply(double a, double b);               // constant-specific body

        String symbol() { return symbol; }
    }

    enum Config {                                                 // best singleton in Java
        INSTANCE;
        private final Map<String, String> values = new java.util.HashMap<>(Map.of("env", "dev"));
        String get(String key) { return values.get(key); }
    }

    public static void main(String[] args) {
        System.out.println("=== Records ===");
        Money a = Money.egp("100.50");
        Money b = Money.egp("20");
        System.out.println("  " + a + " + " + b + " = " + a.add(b));
        System.out.println("  equals by value: " + a.equals(Money.egp("100.50")) + ", accessor amount(): " + a.amount());
        try {
            Money.egp("-1");
        } catch (IllegalArgumentException e) {
            System.out.println("  validation in compact constructor -> " + e.getMessage());
        }

        List<String> names = new ArrayList<>(List.of("Ali"));
        LeakyTeam leaky = new LeakyTeam("A", names);
        Team safe = new Team("B", names);
        names.add("Intruder");
        System.out.println("  LeakyTeam sees outside change: " + leaky.members() + ", Team: " + safe.members());

        System.out.println("\n=== Sealed + pattern matching ===");
        Expr expr = new Add(new Num(2), new Mul(new Num(3), new Neg(new Num(4))));
        System.out.println("  " + show(expr) + " = " + eval(expr));

        System.out.println("\n=== Enums with behaviour ===");
        for (Operation op : Operation.values()) {
            System.out.printf("  6 %s 3 = %.1f%n", op.symbol(), op.apply(6, 3));
        }
        Map<Operation, Integer> usage = new EnumMap<>(Operation.class);
        usage.merge(Operation.PLUS, 1, Integer::sum);
        usage.merge(Operation.PLUS, 1, Integer::sum);
        usage.merge(Operation.TIMES, 1, Integer::sum);
        System.out.println("  EnumMap usage: " + usage);
        System.out.println("  singleton: Config.INSTANCE.get(\"env\") = " + Config.INSTANCE.get("env")
                + ", same instance: " + (Config.INSTANCE == Config.valueOf("INSTANCE")));
    }
}
