package com.jdk.exceptions;

/**
 * Topic 03 - Exceptions: checked vs unchecked, custom exceptions, try/catch/finally.
 */
public class BankAccount {

    /** A custom checked exception: callers must handle or declare it. */
    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    private long balance;

    public BankAccount(long initialBalance) {
        if (initialBalance < 0) {
            // Unchecked: a programming error, not something callers should recover from.
            throw new IllegalArgumentException("initial balance must be >= 0");
        }
        this.balance = initialBalance;
    }

    public void withdraw(long amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (amount > balance) {
            throw new InsufficientFundsException("balance " + balance + " is less than " + amount);
        }
        balance -= amount;
    }

    public long balance() {
        return balance;
    }

    public static void main(String[] args) {
        BankAccount account = new BankAccount(100);
        try {
            account.withdraw(150);
        } catch (InsufficientFundsException e) {
            System.out.println("Could not withdraw: " + e.getMessage());
        } finally {
            System.out.println("Balance is " + account.balance());
        }
    }
}
