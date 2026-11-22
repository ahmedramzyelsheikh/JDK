package com.jdk.security.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Method security: authorization rules next to the business logic, enforced by a proxy (topic 06.09/19).
 */
@Service
public class OrderService {

    public record Order(long id, String owner, BigDecimal total) { }

    private final Map<Long, Order> orders = new ConcurrentHashMap<>(Map.of(
            1L, new Order(1, "ali", new BigDecimal("120.00")),
            2L, new Order(2, "sara", new BigDecimal("75.50")),
            3L, new Order(3, "ali", new BigDecimal("9.99"))));

    /** Object-level authorization: you may only read YOUR order (prevents IDOR / Broken Object Level Authorization). */
    @PostAuthorize("returnObject.owner() == authentication.name or hasRole('ADMIN')")
    public Order get(long id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new NoSuchElementException("order " + id + " not found");
        }
        return order;
    }

    @PreAuthorize("#owner == authentication.name or hasRole('ADMIN')")
    public List<Order> ordersOf(String owner) {
        return orders.values().stream().filter(o -> o.owner().equals(owner)).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(long id) {
        orders.remove(id);
    }
}
