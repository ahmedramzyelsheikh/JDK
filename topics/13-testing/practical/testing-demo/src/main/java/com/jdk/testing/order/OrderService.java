package com.jdk.testing.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * The system under test for the Mockito examples. All collaborators are interfaces injected through the
 * constructor, so tests can replace them with mocks, stubs or fakes.
 */
public class OrderService {

    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("1000");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    public interface ProductCatalog {
        BigDecimal priceOf(String sku);
    }

    public interface PaymentGateway {
        PaymentResult charge(String customerId, BigDecimal amount);
    }

    public interface OrderRepository {
        Order save(Order order);
    }

    public record PaymentResult(boolean approved, String transactionId) {
        public static PaymentResult approved(String tx) {
            return new PaymentResult(true, tx);
        }

        public static PaymentResult declined() {
            return new PaymentResult(false, null);
        }
    }

    public enum Status { PAID, PAYMENT_FAILED }

    public record Order(String customerId, Map<String, Integer> items, BigDecimal total, Status status,
                        String transactionId, Instant createdAt) { }

    public static class EmptyOrderException extends RuntimeException {
        public EmptyOrderException() {
            super("an order needs at least one item");
        }
    }

    private final ProductCatalog catalog;
    private final PaymentGateway payments;
    private final OrderRepository repository;
    private final Clock clock;

    public OrderService(ProductCatalog catalog, PaymentGateway payments, OrderRepository repository, Clock clock) {
        this.catalog = catalog;
        this.payments = payments;
        this.repository = repository;
        this.clock = clock;
    }

    public Order place(String customerId, Map<String, Integer> items) {
        if (items == null || items.isEmpty()) {
            throw new EmptyOrderException();
        }
        BigDecimal total = items.entrySet().stream()
                .map(e -> catalog.priceOf(e.getKey()).multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(DISCOUNT_THRESHOLD) > 0) {
            total = total.subtract(total.multiply(DISCOUNT_RATE));
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        PaymentResult payment = payments.charge(customerId, total);
        Status status = payment.approved() ? Status.PAID : Status.PAYMENT_FAILED;
        return repository.save(new Order(customerId, Map.copyOf(items), total, status, payment.transactionId(), clock.instant()));
    }
}
