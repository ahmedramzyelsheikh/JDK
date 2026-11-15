package com.jdk.testing.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jdk.testing.order.OrderService.EmptyOrderException;
import com.jdk.testing.order.OrderService.Order;
import com.jdk.testing.order.OrderService.OrderRepository;
import com.jdk.testing.order.OrderService.PaymentGateway;
import com.jdk.testing.order.OrderService.PaymentResult;
import com.jdk.testing.order.OrderService.ProductCatalog;
import com.jdk.testing.order.OrderService.Status;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("fast")
@ExtendWith(MockitoExtension.class)            // creates @Mock fields, strict stubs: unused stubbings fail the test
class OrderServiceTest {

    static final Instant NOW = Instant.parse("2026-11-15T10:00:00Z");

    @Mock
    ProductCatalog catalog;                    // STUB: returns canned prices

    @Mock
    PaymentGateway payments;                   // MOCK: we verify how it is called

    @Mock
    OrderRepository repository;

    @Captor
    ArgumentCaptor<Order> savedOrder;

    OrderService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);                 // deterministic time
        service = new OrderService(catalog, payments, repository, fixedClock);
    }

    @Test
    void chargesTheTotalAndSavesAPaidOrder() {
        when(catalog.priceOf("pen")).thenReturn(new BigDecimal("2.50"));
        when(catalog.priceOf("book")).thenReturn(new BigDecimal("40.00"));
        when(payments.charge(anyString(), any())).thenReturn(PaymentResult.approved("tx-42"));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));  // return what was passed

        Order order = service.place("cust-1", Map.of("pen", 4, "book", 1));

        verify(payments).charge(eq("cust-1"), argThat(amount -> amount.compareTo(new BigDecimal("50.00")) == 0));
        verify(repository).save(savedOrder.capture());                        // capture the argument
        assertThat(savedOrder.getValue().status()).isEqualTo(Status.PAID);
        assertThat(savedOrder.getValue().transactionId()).isEqualTo("tx-42");
        assertThat(order.createdAt()).isEqualTo(NOW);
    }

    @Test
    void appliesTenPercentDiscountAboveOneThousand() {
        when(catalog.priceOf("laptop")).thenReturn(new BigDecimal("1200.00"));
        when(payments.charge(anyString(), any())).thenReturn(PaymentResult.approved("tx-1"));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = service.place("cust-1", Map.of("laptop", 1));

        assertThat(order.total()).isEqualByComparingTo("1080.00");
    }

    @Test
    void declinedPaymentIsSavedAsFailed() {
        // BDD style: given / when / then
        given(catalog.priceOf("pen")).willReturn(BigDecimal.ONE);
        given(payments.charge(anyString(), any())).willReturn(PaymentResult.declined());
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Order order = service.place("cust-2", Map.of("pen", 1));

        assertThat(order.status()).isEqualTo(Status.PAYMENT_FAILED);
        then(repository).should().save(argThat(o -> o.transactionId() == null));
    }

    @Test
    void emptyOrderIsRejectedBeforeTouchingAnyCollaborator() {
        assertThatThrownBy(() -> service.place("cust-1", Map.of()))
                .isInstanceOf(EmptyOrderException.class)
                .hasMessageContaining("at least one item");

        verifyNoInteractions(catalog, payments, repository);
    }

    @Test
    void unknownProductStopsTheOrderAndNothingIsCharged() {
        when(catalog.priceOf("ghost")).thenThrow(new IllegalArgumentException("unknown sku ghost"));

        assertThatThrownBy(() -> service.place("cust-1", Map.of("ghost", 1))).hasMessage("unknown sku ghost");

        verify(payments, never()).charge(anyString(), any());
        verify(repository, never()).save(any());
    }

    /** Same behaviour tested with a FAKE repository instead of a mock: state-based, no verify needed. */
    @Nested
    class WithFakeRepository {

        static class InMemoryOrderRepository implements OrderRepository {
            final List<Order> saved = new ArrayList<>();

            @Override
            public Order save(Order order) {
                saved.add(order);
                return order;
            }
        }

        @Test
        void storesTheOrder() {
            InMemoryOrderRepository fake = new InMemoryOrderRepository();
            OrderService withFake = new OrderService(sku -> new BigDecimal("10"),            // lambda stub
                    (customer, amount) -> PaymentResult.approved("tx-9"),                     // lambda stub
                    fake, Clock.fixed(NOW, ZoneOffset.UTC));

            withFake.place("cust-3", Map.of("mug", 3));

            assertThat(fake.saved).singleElement().satisfies(o -> {
                assertThat(o.total()).isEqualByComparingTo("30.00");
                assertThat(o.items()).containsEntry("mug", 3);
            });
        }
    }
}
