import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Checked vs unchecked, catch order, multi-catch, finally flow, and finally corner cases.
 * Run: java ExceptionBasics.java
 */
public class ExceptionBasics {

    public static void main(String[] args) {
        System.out.println("=== Common unchecked exceptions ===");
        List<Runnable> bugs = List.of(
                () -> { String s = null; s.length(); },
                () -> Integer.parseInt("12a"),
                () -> { int[] a = new int[2]; a[5] = 1; },
                () -> { Object o = "x"; Integer i = (Integer) o; },
                () -> System.out.println(1 / zero()),
                () -> List.of(1).add(2),
                () -> List.<String>of().iterator().next());
        for (Runnable bug : bugs) {
            try {
                bug.run();
            } catch (RuntimeException e) {
                System.out.printf("  %-40s %s%n", e.getClass().getSimpleName(), e.getMessage());
            }
        }

        System.out.println("\n=== Checked exceptions must be caught or declared ===");
        try {
            readConfig("missing.properties");
        } catch (FileNotFoundException e) {          // more specific first
            System.out.println("  FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("  IOException: " + e.getMessage());
        }

        System.out.println("\n=== Multi-catch ===");
        for (String input : new String[] {"abc", "0"}) {
            try {
                int n = Integer.parseInt(input);
                System.out.println(10 / n);
            } catch (NumberFormatException | ArithmeticException e) {
                System.out.println("  input '" + input + "' -> " + e.getClass().getSimpleName());
            }
        }

        System.out.println("\n=== finally flow ===");
        System.out.println("  result = " + tryReturn());
        System.out.println("  return in finally overrides: " + finallyOverrides());
        System.out.println("  return in finally swallows exception: " + finallySwallows());
        try {
            finallyMasks();
        } catch (RuntimeException e) {
            System.out.println("  exception from finally replaced the original: " + e.getMessage());
        }

        System.out.println("\n=== Helpful NullPointerException message (Java 14+) ===");
        try {
            Customer c = new Customer(null);
            System.out.println(c.address.toUpperCase());
        } catch (NullPointerException e) {
            System.out.println("  " + e.getMessage());
        }
    }

    record Customer(String address) { }

    static int zero() {
        return 0;
    }

    static void readConfig(String file) throws IOException {
        throw new FileNotFoundException(file + " (simulated)");
    }

    static String tryReturn() {
        try {
            System.out.println("  try block returns...");
            return "from try";
        } finally {
            System.out.println("  ...but finally runs first");
        }
    }

    @SuppressWarnings("finally")
    static String finallyOverrides() {
        try {
            return "try";
        } finally {
            return "finally";                       // never do this
        }
    }

    @SuppressWarnings("finally")
    static String finallySwallows() {
        try {
            throw new IllegalStateException("important error");
        } finally {
            return "no exception seen!";            // the exception disappears
        }
    }

    static void finallyMasks() {
        try {
            throw new IllegalStateException("ORIGINAL error");
        } finally {
            throw new RuntimeException("error in finally");
        }
    }
}
