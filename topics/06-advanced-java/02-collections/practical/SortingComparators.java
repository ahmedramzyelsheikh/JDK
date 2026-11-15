import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Comparable (natural order) vs Comparator (any order), chained comparators, nulls,
 * stability, the subtraction-overflow bug, and compareTo inconsistent with equals.
 * Run: java SortingComparators.java
 */
public class SortingComparators {

    record Employee(String name, String department, int salary) implements Comparable<Employee> {
        @Override
        public int compareTo(Employee other) {          // natural order: by name
            return name.compareTo(other.name);
        }
    }

    public static void main(String[] args) {
        List<Employee> staff = new ArrayList<>(List.of(
                new Employee("Sara", "IT", 12_000),
                new Employee("Ali", "HR", 7_000),
                new Employee("Omar", "IT", 9_000),
                new Employee("Mona", "HR", 7_000),
                new Employee("Hany", "IT", 12_000)));

        Collections.sort(staff);                         // uses Comparable
        print("natural order (name)", staff);

        staff.sort(Comparator.comparingInt(Employee::salary));
        print("by salary (stable: equal salaries keep previous name order)", staff);

        staff.sort(Comparator.comparing(Employee::department)
                .thenComparing(Employee::salary, Comparator.reverseOrder())
                .thenComparing(Employee::name));
        print("department, then salary desc, then name", staff);

        System.out.println("\n=== nulls ===");
        List<String> withNulls = new ArrayList<>(List.of("b", "a", "c"));
        withNulls.add(1, null);
        withNulls.sort(Comparator.nullsLast(Comparator.naturalOrder()));
        System.out.println("  nullsLast: " + withNulls);

        System.out.println("\n=== The subtraction bug ===");
        Comparator<Integer> wrong = (a, b) -> a - b;     // overflows!
        Comparator<Integer> right = Integer::compare;
        int big = Integer.MAX_VALUE, negative = -10;
        System.out.println("  wrong.compare(MAX, -10) = " + wrong.compare(big, negative) + "  (says MAX < -10!)");
        System.out.println("  right.compare(MAX, -10) = " + right.compare(big, negative));

        System.out.println("\n=== compareTo inconsistent with equals ===");
        BigDecimal a = new BigDecimal("2.0");
        BigDecimal b = new BigDecimal("2.00");
        Set<BigDecimal> hashSet = new HashSet<>(List.of(a, b));
        Set<BigDecimal> treeSet = new TreeSet<>(List.of(a, b));
        System.out.println("  equals: " + a.equals(b) + ", compareTo: " + a.compareTo(b));
        System.out.println("  HashSet size " + hashSet.size() + " vs TreeSet size " + treeSet.size());
    }

    static void print(String title, List<Employee> list) {
        System.out.println("\n" + title + ":");
        list.forEach(e -> System.out.printf("  %-5s %-3s %,7d%n", e.name(), e.department(), e.salary()));
    }
}
