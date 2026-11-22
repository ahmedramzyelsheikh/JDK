package com.jdk.security.api;

import com.jdk.security.api.OrderService.Order;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    private final OrderService orders;

    public ApiController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping("/api/public/hello")
    public Map<String, String> hello() {
        return Map.of("message", "anyone can read this");
    }

    /** The validated token is available as the principal. */
    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("username", jwt.getSubject(), "roles", jwt.getClaimAsStringList("roles"),
                "expiresAt", String.valueOf(jwt.getExpiresAt()));
    }

    @GetMapping("/api/orders/{id}")
    public Order order(@PathVariable long id) {
        return orders.get(id);
    }

    @GetMapping("/api/users/{username}/orders")
    public List<Order> ordersOf(@PathVariable String username) {
        return orders.ordersOf(username);
    }

    @DeleteMapping("/api/orders/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        orders.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/stats")
    public Map<String, Object> adminStats() {
        return Map.of("orders", 3, "note", "only ADMIN reaches this (URL rule)");
    }

    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail notFound(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
