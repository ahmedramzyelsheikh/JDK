import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulation, validation, leaking internal state, defensive copies and immutable classes.
 * Run: java Encapsulation.java
 */
public class Encapsulation {

    static class BankAccount {
        private BigDecimal balance = BigDecimal.ZERO;
        private final List<String> history = new ArrayList<>();

        void deposit(BigDecimal amount) {
            if (amount.signum() <= 0) {
                throw new IllegalArgumentException("deposit must be positive");
            }
            balance = balance.add(amount);
            history.add("deposit " + amount);
        }

        void withdraw(BigDecimal amount) {
            if (amount.compareTo(balance) > 0) {
                throw new IllegalStateException("insufficient funds");
            }
            balance = balance.subtract(amount);
            history.add("withdraw " + amount);
        }

        BigDecimal getBalance() {
            return balance;                          // BigDecimal is immutable: safe to return
        }

        List<String> leakyHistory() {
            return history;                          // BAD: caller can modify our internal list
        }

        List<String> getHistory() {
            return List.copyOf(history);             // GOOD: unmodifiable copy
        }

        boolean sameBalanceAs(BankAccount other) {
            return balance.equals(other.balance);    // private is per CLASS: we can read other's field
        }
    }

    /** Immutable class: final, private final fields, no setters, defensive copies, "with" methods. */
    static final class Order {
        private final String id;
        private final List<String> items;
        private final LocalDate date;

        Order(String id, List<String> items, LocalDate date) {
            this.id = id;
            this.items = List.copyOf(items);         // defensive copy IN (caller keeps its own list)
            this.date = date;                        // LocalDate is immutable: no copy needed
        }

        List<String> items() {
            return items;                            // already unmodifiable
        }

        Order withItem(String item) {
            List<String> copy = new ArrayList<>(items);
            copy.add(item);
            return new Order(id, copy, date);        // new object instead of modifying
        }

        @Override
        public String toString() {
            return "Order " + id + " " + items + " on " + date;
        }
    }

    public static void main(String[] args) {
        BankAccount account = new BankAccount();
        account.deposit(new BigDecimal("100.00"));
        account.withdraw(new BigDecimal("30.50"));
        System.out.println("balance: " + account.getBalance() + ", history: " + account.getHistory());

        try {
            account.deposit(new BigDecimal("-5"));
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            account.getHistory().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("getHistory().clear() -> UnsupportedOperationException (protected)");
        }
        account.leakyHistory().clear();
        System.out.println("after leakyHistory().clear(): history = " + account.getHistory() + "  <- state leaked!");

        BankAccount other = new BankAccount();
        other.deposit(new BigDecimal("69.50"));
        System.out.println("same balance (reads other's private field): " + account.sameBalanceAs(other));

        System.out.println("\n=== Immutable Order ===");
        List<String> input = new ArrayList<>(List.of("book"));
        Order order = new Order("A-1", input, LocalDate.of(2026, 11, 15));
        input.add("hacked");                         // doesn't affect the order (defensive copy)
        Order bigger = order.withItem("pen");
        System.out.println(order);
        System.out.println(bigger);
        try {
            order.items().add("x");
        } catch (UnsupportedOperationException e) {
            System.out.println("order.items().add -> UnsupportedOperationException");
        }
    }
}
