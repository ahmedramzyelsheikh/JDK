import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * try-with-resources (close order, suppressed exceptions), custom checked/unchecked exceptions,
 * exception chaining/translation, and checked exceptions inside lambdas.
 * Run: java ResourcesAndCustomExceptions.java
 */
public class ResourcesAndCustomExceptions {

    // ---------- resources ----------
    static class Resource implements AutoCloseable {
        private final String name;
        private final boolean failOnClose;

        Resource(String name, boolean failOnClose) {
            this.name = name;
            this.failOnClose = failOnClose;
            System.out.println("  open  " + name);
        }

        @Override
        public void close() {
            System.out.println("  close " + name);
            if (failOnClose) {
                throw new IllegalStateException("close failed for " + name);
            }
        }
    }

    // ---------- custom exceptions ----------
    /** Checked: the caller is expected to handle it (e.g. show a message, offer a smaller amount). */
    static class InsufficientFundsException extends Exception {
        private final BigDecimal missing;

        InsufficientFundsException(String accountId, BigDecimal balance, BigDecimal requested) {
            super("Account %s has %s, requested %s".formatted(accountId, balance, requested));
            this.missing = requested.subtract(balance);
        }

        BigDecimal missing() {
            return missing;
        }
    }

    /** Unchecked: wraps low-level failures at the repository boundary. */
    static class RepositoryException extends RuntimeException {
        RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class Account {
        private final String id;
        private BigDecimal balance;

        Account(String id, String balance) {
            this.id = id;
            this.balance = new BigDecimal(balance);
        }

        void withdraw(BigDecimal amount) throws InsufficientFundsException {
            if (amount.signum() <= 0) {
                throw new IllegalArgumentException("amount must be positive: " + amount);   // fail fast
            }
            if (amount.compareTo(balance) > 0) {
                throw new InsufficientFundsException(id, balance, amount);
            }
            balance = balance.subtract(amount);
        }
    }

    static void saveToDatabase(String order) {
        try {
            throw new SQLException("duplicate key value violates unique constraint", "23505");
        } catch (SQLException e) {
            throw new RepositoryException("could not save order " + order, e);   // translate + keep cause
        }
    }

    static String readFile(String name) throws IOException {
        if (name.startsWith("bad")) {
            throw new IOException("cannot read " + name);
        }
        return "content of " + name;
    }

    public static void main(String[] args) {
        System.out.println("=== try-with-resources: reverse close order ===");
        try (Resource db = new Resource("db", false); Resource file = new Resource("file", false)) {
            System.out.println("  using " + db.name + " and " + file.name);
        }

        System.out.println("\n=== Body fails AND close fails -> suppressed ===");
        try (Resource r = new Resource("socket", true)) {
            throw new IllegalArgumentException("body failed");
        } catch (IllegalArgumentException e) {
            System.out.println("  caught: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) {
                System.out.println("  suppressed: " + s.getMessage());
            }
        }

        System.out.println("\n=== Custom checked exception ===");
        Account account = new Account("ACC-1", "100");
        try {
            account.withdraw(new BigDecimal("250"));
        } catch (InsufficientFundsException e) {
            System.out.println("  " + e.getMessage() + " (missing " + e.missing() + ")");
        }
        try {
            account.withdraw(new BigDecimal("-1"));
        } catch (IllegalArgumentException | InsufficientFundsException e) {
            System.out.println("  " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        System.out.println("\n=== Translation and cause chain ===");
        try {
            saveToDatabase("order-42");
        } catch (RepositoryException e) {
            Throwable t = e;
            int level = 0;
            while (t != null) {
                System.out.println("  " + "  ".repeat(level++) + t.getClass().getSimpleName() + ": " + t.getMessage());
                t = t.getCause();
            }
        }

        System.out.println("\n=== Checked exceptions inside lambdas ===");
        List<String> files = List.of("a.txt", "bad.txt", "c.txt");
        try {
            files.stream()
                    .map(f -> {
                        try {
                            return readFile(f);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);       // lambdas can't throw checked exceptions
                        }
                    })
                    .forEach(content -> System.out.println("  " + content));
        } catch (UncheckedIOException e) {
            System.out.println("  stream stopped: " + e.getCause().getMessage());
        }

        System.out.println("\n=== InterruptedException: restore the flag ===");
        Thread.currentThread().interrupt();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();                     // don't swallow the interrupt
            System.out.println("  interrupted, flag restored: " + Thread.currentThread().isInterrupted());
        }
    }
}
