package com.jdk.generics;

import java.util.List;
import java.util.function.Function;

/**
 * Topic 05 - Generics: generic classes, generic methods, bounded types, wildcards.
 */
public record Box<T>(T value) {

    /** A generic method that changes the type inside the box. */
    public <R> Box<R> map(Function<? super T, ? extends R> mapper) {
        return new Box<>(mapper.apply(value));
    }

    /** Bounded type parameter: T must be comparable to itself. */
    public static <T extends Comparable<T>> T max(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        T best = items.get(0);
        for (T item : items) {
            if (item.compareTo(best) > 0) {
                best = item;
            }
        }
        return best;
    }

    /** Upper-bounded wildcard: accepts List<Integer>, List<Double>, ... ("producer extends"). */
    public static double sum(List<? extends Number> numbers) {
        double total = 0;
        for (Number number : numbers) {
            total += number.doubleValue();
        }
        return total;
    }

    public static void main(String[] args) {
        Box<String> name = new Box<>("java");
        Box<Integer> length = name.map(String::length);
        System.out.println(name + " -> " + length);
        System.out.println("Max: " + max(List.of(3, 7, 5)));
        System.out.println("Sum: " + sum(List.of(1, 2.5, 3L)));
    }
}
