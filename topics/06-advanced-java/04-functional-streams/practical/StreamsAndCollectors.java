import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Stream sources, intermediate/terminal operations, laziness, flatMap, reduce, Collectors,
 * parallel streams and pitfalls, on a small employee dataset.
 * Run: java StreamsAndCollectors.java
 */
public class StreamsAndCollectors {

    record Employee(String name, String dept, int salary, List<String> skills) { }

    static final List<Employee> STAFF = List.of(
            new Employee("Ali", "IT", 12_000, List.of("Java", "SQL")),
            new Employee("Sara", "IT", 15_000, List.of("Java", "Spring", "Docker")),
            new Employee("Omar", "HR", 7_000, List.of("Excel")),
            new Employee("Mona", "Finance", 9_500, List.of("SQL", "Excel")),
            new Employee("Hany", "IT", 8_000, List.of("JavaScript")),
            new Employee("Nour", "Finance", 11_000, List.of("Python", "SQL")));

    public static void main(String[] args) {
        System.out.println("=== filter / map / sorted / toList ===");
        List<String> wellPaid = STAFF.stream()
                .filter(e -> e.salary() > 9_000)
                .sorted(Comparator.comparingInt(Employee::salary).reversed())
                .map(e -> e.name().toUpperCase())
                .toList();
        System.out.println("  salary > 9000, highest first: " + wellPaid);

        System.out.println("\n=== Laziness: vertical processing + short-circuit ===");
        List<Integer> firstTwo = Stream.of(1, 2, 3, 4, 5, 6)
                .filter(n -> { System.out.println("    filter " + n); return n % 2 == 0; })
                .map(n -> { System.out.println("    map    " + n); return n * 10; })
                .limit(2)
                .toList();
        System.out.println("  result " + firstTwo + " (5 and 6 were never touched)");
        Stream<String> notRun = Stream.of("x").peek(s -> System.out.println("never printed"));
        System.out.println("  a pipeline without a terminal operation does nothing (peek printed nothing): " + (notRun != null));

        System.out.println("\n=== Sources ===");
        System.out.println("  range: " + IntStream.rangeClosed(1, 5).boxed().toList()
                + ", iterate: " + Stream.iterate(1, x -> x < 200, x -> x * 3).toList()
                + ", chars: " + "abc".chars().mapToObj(c -> (char) c).toList());

        System.out.println("\n=== flatMap / distinct ===");
        Set<String> allSkills = STAFF.stream().flatMap(e -> e.skills().stream()).collect(Collectors.toCollection(java.util.TreeSet::new));
        System.out.println("  all skills (sorted, unique): " + allSkills);

        System.out.println("\n=== Numbers: sum / average / statistics / reduce ===");
        int total = STAFF.stream().mapToInt(Employee::salary).sum();
        IntSummaryStatistics stats = STAFF.stream().mapToInt(Employee::salary).summaryStatistics();
        int product = IntStream.rangeClosed(1, 5).reduce(1, (a, b) -> a * b);
        System.out.println("  total " + total + ", stats " + stats + ", 5! via reduce = " + product);

        System.out.println("\n=== Matching / finding ===");
        System.out.println("  anyMatch HR: " + STAFF.stream().anyMatch(e -> e.dept().equals("HR"))
                + ", allMatch salary>5000: " + STAFF.stream().allMatch(e -> e.salary() > 5_000)
                + ", noneMatch Rust: " + STAFF.stream().noneMatch(e -> e.skills().contains("Rust")));
        Optional<Employee> firstSql = STAFF.stream().filter(e -> e.skills().contains("SQL")).findFirst();
        System.out.println("  first with SQL: " + firstSql.map(Employee::name).orElse("none"));

        System.out.println("\n=== Collectors ===");
        Map<String, Long> countByDept = STAFF.stream().collect(groupingBy(Employee::dept, TreeMap::new, counting()));
        Map<String, Double> avgByDept = STAFF.stream().collect(groupingBy(Employee::dept, TreeMap::new, averagingInt(Employee::salary)));
        Map<String, Set<String>> namesByDept = STAFF.stream().collect(groupingBy(Employee::dept, TreeMap::new, mapping(Employee::name, toSet())));
        Map<Boolean, List<String>> partition = STAFF.stream().collect(partitioningBy(e -> e.salary() >= 10_000, mapping(Employee::name, Collectors.toList())));
        Map<String, Integer> maxByDept = STAFF.stream().collect(toMap(Employee::dept, Employee::salary, Integer::max, TreeMap::new));
        String names = STAFF.stream().map(Employee::name).collect(joining(", ", "[", "]"));
        String range = STAFF.stream().collect(Collectors.teeing(
                Collectors.minBy(Comparator.comparingInt(Employee::salary)),
                Collectors.maxBy(Comparator.comparingInt(Employee::salary)),
                (min, max) -> min.get().name() + ".." + max.get().name()));
        System.out.println("  count by dept:   " + countByDept);
        System.out.println("  avg by dept:     " + avgByDept);
        System.out.println("  names by dept:   " + namesByDept);
        System.out.println("  >= 10000 split:  " + partition);
        System.out.println("  max by dept:     " + maxByDept);
        System.out.println("  joining:         " + names);
        System.out.println("  teeing min..max: " + range);

        System.out.println("\n=== Top 2 skills by how many employees have them ===");
        STAFF.stream()
                .flatMap(e -> e.skills().stream())
                .collect(groupingBy(s -> s, counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(2)
                .forEach(e -> System.out.println("  " + e.getKey() + " x" + e.getValue()));

        System.out.println("\n=== Pitfalls ===");
        try {
            STAFF.stream().collect(toMap(Employee::dept, Employee::name));
        } catch (IllegalStateException e) {
            System.out.println("  toMap with duplicate keys -> " + e.getMessage());
        }
        Stream<Employee> once = STAFF.stream();
        once.count();
        try {
            once.count();
        } catch (IllegalStateException e) {
            System.out.println("  reusing a stream -> " + e.getMessage());
        }

        System.out.println("\n=== Parallel ===");
        long[] count = new long[2];
        long sequential = time(() -> count[0] = IntStream.rangeClosed(1, 3_000_000).filter(StreamsAndCollectors::isPrime).count());
        long parallel = time(() -> count[1] = IntStream.rangeClosed(1, 3_000_000).parallel().filter(StreamsAndCollectors::isPrime).count());
        System.out.printf("  count primes <= 3M (%d = %d): sequential %d ms, parallel %d ms on %d cores%n",
                count[0], count[1], sequential, parallel, Runtime.getRuntime().availableProcessors());
        long t0 = System.nanoTime();
        IntStream.range(0, 1_000).sum();
        long t1 = System.nanoTime();
        IntStream.range(0, 1_000).parallel().sum();
        long t2 = System.nanoTime();
        System.out.printf("  tiny sum of 1000 ints: sequential %d us, parallel %d us (overhead > gain)%n",
                (t1 - t0) / 1_000, (t2 - t1) / 1_000);
    }

    static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        for (int d = 2; (long) d * d <= n; d++) {
            if (n % d == 0) {
                return false;
            }
        }
        return true;
    }

    static long time(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return (System.nanoTime() - start) / 1_000_000;
    }
}
