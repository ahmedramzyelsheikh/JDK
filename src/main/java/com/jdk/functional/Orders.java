package com.jdk.functional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Topic 06 - Lambdas, functional interfaces, Streams and Optional.
 */
public final class Orders {

    private Orders() {
    }

    public record Order(String customer, String product, double price, int quantity) {
        public double total() {
            return price * quantity;
        }
    }

    /** filter + mapToDouble + sum. */
    public static double revenue(List<Order> orders) {
        return orders.stream().mapToDouble(Order::total).sum();
    }

    /** groupingBy with a downstream collector. */
    public static Map<String, Double> revenueByCustomer(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::customer, Collectors.summingDouble(Order::total)));
    }

    /** Optional instead of returning null. */
    public static Optional<Order> biggestOrder(List<Order> orders) {
        return orders.stream().max((a, b) -> Double.compare(a.total(), b.total()));
    }

    /** filter + map + distinct + sorted + toList. */
    public static List<String> productsOver(List<Order> orders, double minPrice) {
        return orders.stream()
                .filter(o -> o.price() > minPrice)
                .map(Order::product)
                .distinct()
                .sorted()
                .toList();
    }

    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order("ali", "laptop", 900, 1),
                new Order("sara", "mouse", 20, 3),
                new Order("ali", "monitor", 250, 2));
        System.out.println("Revenue: " + revenue(orders));
        System.out.println("By customer: " + revenueByCustomer(orders));
        System.out.println("Biggest: " + biggestOrder(orders).map(Order::product).orElse("none"));
        System.out.println("Over 100: " + productsOver(orders, 100));
    }
}
