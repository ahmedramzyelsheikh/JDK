import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * One feature from each important Java release. Requires Java 21+.
 *
 * Run: java topics/01-history/practical/ModernJavaTour.java
 */
public class ModernJavaTour {

    // Java 5: enum, generics, annotations
    enum Level { BEGINNER, INTERMEDIATE, ADVANCED }

    // Java 16: record  /  Java 17: sealed interface
    sealed interface Shape permits Circle, Square {}
    record Circle(double r) implements Shape {}
    record Square(double side) implements Shape {}

    public static void main(String[] args) throws Exception {
        System.out.println("Running on Java " + Runtime.version() + " by " + System.getProperty("java.vendor"));
        System.out.println();

        // Java 5: generics + enhanced for + autoboxing
        List<Integer> numbers = new ArrayList<>();      // Java 7: diamond <>
        for (int i = 1; i <= 5; i++) {
            numbers.add(i);                              // int autoboxed to Integer
        }
        print("Java 5  generics/autoboxing", numbers);

        // Java 7: strings in switch + try-with-resources (see 07-io topic)
        String day = "SAT";
        switch (day) {
            case "SAT", "SUN" -> print("Java 7/14 switch on String", "weekend");
            default -> print("Java 7/14 switch on String", "weekday");
        }

        // Java 8: lambdas, streams, Optional, java.time
        int sumOfSquares = numbers.stream().mapToInt(n -> n * n).sum();
        print("Java 8  lambda + stream", sumOfSquares);
        print("Java 8  Optional", Optional.ofNullable(null).orElse("default value"));
        print("Java 8  java.time", LocalDate.of(1996, 1, 23).plusYears(30).getDayOfWeek());

        // Java 9: immutable collection factories
        Map<String, Integer> lts = Map.of("Java 8", 2014, "Java 11", 2018, "Java 17", 2021, "Java 21", 2023);
        print("Java 9  Map.of", lts.size() + " LTS entries");

        // Java 10: var
        var message = "type inferred as String";
        print("Java 10 var", message);

        // Java 11: String helpers + standard HttpClient
        print("Java 11 String.repeat/isBlank", "ab".repeat(3) + " / " + "   ".isBlank());
        HttpClient client = HttpClient.newHttpClient();
        print("Java 11 HttpClient created", client.version() + " (target " + URI.create("https://openjdk.org") + ")");

        // Java 14: switch expression yielding a value
        Level level = Level.INTERMEDIATE;
        int months = switch (level) {
            case BEGINNER -> 3;
            case INTERMEDIATE -> 6;
            case ADVANCED -> 12;
        };
        print("Java 14 switch expression", months + " months");

        // Java 15: text block
        String json = """
                {"language": "Java", "born": 1995}
                """.strip();
        print("Java 15 text block", json);

        // Java 16: pattern matching for instanceof
        Object o = "pattern";
        if (o instanceof String s && s.length() > 3) {
            print("Java 16 instanceof pattern", s.toUpperCase());
        }

        // Java 17 + 21: sealed types + pattern matching for switch + record patterns
        List<Shape> shapes = List.of(new Circle(1), new Square(2));
        String areas = shapes.stream().map(ModernJavaTour::area).map("%.2f"::formatted).collect(Collectors.joining(", "));
        print("Java 21 switch + record patterns", areas);

        // Java 21: sequenced collections
        SequencedCollection<Integer> seq = new ArrayList<>(numbers);
        print("Java 21 getFirst/getLast/reversed", seq.getFirst() + " " + seq.getLast() + " " + seq.reversed());

        // Java 21: virtual threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var future = executor.submit(() -> Thread.currentThread().isVirtual());
            print("Java 21 virtual thread?", future.get());
        }
    }

    static double area(Shape shape) {
        return switch (shape) {
            case Circle(double r) -> Math.PI * r * r;
            case Square(double side) -> side * side;
        };
    }

    static void print(String label, Object value) {
        System.out.printf("%-36s -> %s%n", label, value);
    }
}
