import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A tour of Java 14-21 features in data-oriented style: records, sealed types, switch expressions,
 * pattern matching with record patterns and guards, text blocks, sequenced collections and newer APIs.
 * Run: java ModernJava21.java
 */
public class ModernJava21 {

    // ---------- data modelled as sealed records ----------
    sealed interface Payment permits Card, Wallet, Cash { }
    record Card(String holder, String number, Money amount) implements Payment {
        Card {
            if (number.length() != 16) {
                throw new IllegalArgumentException("card number must have 16 digits");
            }
        }
        String masked() {
            return "**** " + number.substring(12);
        }
    }
    record Wallet(String provider, Money amount) implements Payment { }
    record Cash(Money amount) implements Payment { }
    record Money(double value, String currency) { }

    static String describe(Payment p) {
        return switch (p) {                                                        // exhaustive, no default
            case Card c when c.amount().value() > 10_000 -> "large card payment by " + c.holder() + " needs approval";
            case Card c -> "card " + c.masked() + " pays " + fmt(c.amount());
            case Wallet(String provider, Money(double v, String cur)) when cur.equals("EGP") -> provider + " wallet pays " + v + " pounds";
            case Wallet(var provider, var amount) -> provider + " wallet pays " + fmt(amount);
            case Cash(Money(var value, var currency)) -> "cash " + value + " " + currency;  // nested record pattern
        };
    }

    static double fee(Payment p) {
        return switch (p) {
            case Card c -> c.amount().value() * 0.02;
            case Wallet w -> 1.5;
            case Cash c -> 0;
        };
    }

    static String fmt(Money m) {
        return "%.2f %s".formatted(m.value(), m.currency());
    }

    // ---------- pattern matching over any object ----------
    static String classify(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer i when i < 0 -> "negative int";
            case Integer i -> "int " + i;
            case String s when s.isBlank() -> "blank string";
            case String s -> "string \"" + s + "\"";
            case int[] arr -> "int array of " + arr.length;
            case List<?> list -> "list of size " + list.size();
            default -> o.getClass().getSimpleName();
        };
    }

    public static void main(String[] args) {
        System.out.println("=== Sealed records + pattern matching ===");
        List<Payment> payments = List.of(
                new Card("Ali", "4111111111111111", new Money(250, "USD")),
                new Card("Sara", "5500000000000004", new Money(20_000, "USD")),
                new Wallet("Vodafone Cash", new Money(300, "EGP")),
                new Wallet("PayPal", new Money(80, "EUR")),
                new Cash(new Money(50, "EGP")));
        for (Payment p : payments) {
            System.out.printf("  %-58s fee %.2f%n", describe(p), fee(p));
        }

        System.out.println("\n=== Classify anything ===");
        for (Object o : new Object[] {-5, 7, " ", "hi", new int[3], List.of(1, 2), 3.14, null}) {
            System.out.println("  " + classify(o));
        }

        System.out.println("\n=== instanceof pattern ===");
        Object value = "  modern java  ";
        if (value instanceof String s && !s.isBlank()) {
            System.out.println("  stripped: '" + s.strip() + "'");
        }

        System.out.println("\n=== Text block ===");
        String query = """
                SELECT name, salary \
                FROM employee
                WHERE dept = 'IT'\s
                ORDER BY salary DESC
                """;
        System.out.print(query.indent(2));

        System.out.println("=== Switch expression with yield ===");
        for (String day : List.of("FRI", "SAT", "MON")) {
            int hours = switch (day) {
                case "FRI", "SAT" -> 0;
                default -> {
                    int base = 8;
                    yield day.equals("MON") ? base + 1 : base;
                }
            };
            System.out.print("  " + day + "=" + hours + "h");
        }
        System.out.println();

        System.out.println("\n=== Sequenced collections ===");
        var list = new ArrayList<>(List.of("b", "c"));
        list.addFirst("a");
        list.addLast("d");
        System.out.println("  " + list + " first=" + list.getFirst() + " last=" + list.getLast() + " reversed=" + list.reversed());
        SequencedMap<String, Integer> ranking = new LinkedHashMap<>();
        ranking.put("Sara", 1);
        ranking.put("Ali", 2);
        ranking.putFirst("Omar", 0);
        System.out.println("  map " + ranking + " firstEntry=" + ranking.firstEntry() + " lastEntry=" + ranking.lastEntry());

        System.out.println("\n=== Newer APIs ===");
        System.out.println("  Math.clamp(150, 0, 100) = " + Math.clamp(150, 0, 100));
        System.out.println("  StringBuilder.repeat: " + new StringBuilder().repeat("ab", 3));
        System.out.println("  splitWithDelimiters: " + Arrays.toString("a,b;c".splitWithDelimiters("[,;]", 0)));
        System.out.println("  HexFormat: " + HexFormat.of().formatHex(new byte[] {10, 20, (byte) 255}));
        System.out.println("  RandomGenerator L64X128MixRandom dice roll: " + RandomGenerator.of("L64X128MixRandom").nextInt(1, 7) + " (dice)");
        System.out.println("  Stream.toList + teeing: " + Stream.of(5, 3, 9, 1).collect(Collectors.teeing(
                Collectors.minBy(Integer::compare), Collectors.maxBy(Integer::compare),
                (min, max) -> min.orElseThrow() + ".." + max.orElseThrow())));
        System.out.println("  String.transform: " + "java".transform(s -> s.toUpperCase() + "!"));

        System.out.println("\n=== Records validate themselves ===");
        try {
            new Card("X", "123", new Money(1, "USD"));
        } catch (IllegalArgumentException e) {
            System.out.println("  " + e.getMessage());
        }
    }
}
