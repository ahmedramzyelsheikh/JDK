package com.jdk.functional;

import static org.assertj.core.api.Assertions.assertThat;

import com.jdk.functional.Orders.Order;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrdersTest {

    private final List<Order> orders = List.of(
            new Order("ali", "laptop", 900, 1),
            new Order("sara", "mouse", 20, 3),
            new Order("ali", "monitor", 250, 2));

    @Test
    void computesRevenue() {
        assertThat(Orders.revenue(orders)).isEqualTo(1460.0);
    }

    @Test
    void groupsRevenueByCustomer() {
        assertThat(Orders.revenueByCustomer(orders))
                .containsEntry("ali", 1400.0)
                .containsEntry("sara", 60.0);
    }

    @Test
    void findsBiggestOrderOrEmpty() {
        assertThat(Orders.biggestOrder(orders)).get().extracting(Order::product).isEqualTo("laptop");
        assertThat(Orders.biggestOrder(List.of())).isEmpty();
    }

    @Test
    void listsSortedProductsOverPrice() {
        assertThat(Orders.productsOver(orders, 100)).containsExactly("laptop", "monitor");
    }
}
