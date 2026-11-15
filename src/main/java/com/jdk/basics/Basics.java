package com.jdk.basics;

/**
 * Topic 01 - Java basics: types, operators, control flow, methods, arrays, strings.
 */
public final class Basics {

    private Basics() {
    }

    /** Loops and conditionals: sum of the even numbers from 1 to n. */
    public static int sumOfEvens(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            if (i % 2 == 0) {
                sum += i;
            }
        }
        return sum;
    }

    /** Arrays: largest value in a non-empty array. */
    public static int max(int[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values must not be empty");
        }
        int max = values[0];
        for (int value : values) {
            max = Math.max(max, value);
        }
        return max;
    }

    /** Strings: true if the text reads the same backwards, ignoring case and non-letters. */
    public static boolean isPalindrome(String text) {
        String clean = text.toLowerCase().replaceAll("[^a-z0-9]", "");
        return new StringBuilder(clean).reverse().toString().equals(clean);
    }

    /** Switch expressions (Java 14+). */
    public static String dayType(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 6, 7 -> "weekend";
            case 1, 2, 3, 4, 5 -> "weekday";
            default -> throw new IllegalArgumentException("day must be 1-7: " + dayOfWeek);
        };
    }

    public static void main(String[] args) {
        System.out.println("Sum of evens to 10: " + sumOfEvens(10));
        System.out.println("Max: " + max(new int[] {3, 9, 4}));
        System.out.println("Palindrome? " + isPalindrome("Never odd or even"));
        System.out.println("Day 6 is a " + dayType(6));
    }
}
