package com.jdk.logging;

import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/** Shows good logging practice: levels, placeholders, exceptions, MDC, masking, the fluent API. */
public class OrderProcessor {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);   // one static logger per class

    public record Order(long id, String customerEmail, String cardNumber, BigDecimal amount) { }

    public static class PaymentDeclinedException extends RuntimeException {
        public PaymentDeclinedException(String message) {
            super(message);
        }
    }

    /** Entry point of a "request": everything logged inside carries the same requestId via MDC. */
    public boolean handle(Order order) {
        MDC.put("requestId", UUID.randomUUID().toString().substring(0, 8));
        try {
            log.info("Processing order {} for {} amount {}", order.id(), maskEmail(order.customerEmail()), order.amount());
            log.debug("Paying with card {}", maskCard(order.cardNumber()));                    // never the full number
            log.atDebug().setMessage("Risk score computed: {}").addArgument(() -> riskScore(order)).log();  // lazy argument

            charge(order);
            log.atInfo().addKeyValue("orderId", order.id()).addKeyValue("amount", order.amount()).log("Order paid");
            return true;
        } catch (PaymentDeclinedException e) {
            log.warn("Payment declined for order {}: {}", order.id(), e.getMessage());          // expected business case
            return false;
        } catch (RuntimeException e) {
            log.error("Unexpected failure processing order {}", order.id(), e);                 // exception LAST -> stack trace
            return false;
        } finally {
            MDC.clear();                                                                       // threads are reused!
        }
    }

    private void charge(Order order) {
        if (order.amount().compareTo(new BigDecimal("10000")) > 0) {
            throw new PaymentDeclinedException("amount above card limit");
        }
        if (order.cardNumber().startsWith("0000")) {
            throw new IllegalStateException("payment provider returned malformed response");
        }
    }

    private int riskScore(Order order) {
        return order.amount().intValue() % 100;            // pretend this is expensive
    }

    static String maskCard(String card) {
        return card == null || card.length() < 4 ? "****" : "**** **** **** " + card.substring(card.length() - 4);
    }

    static String maskEmail(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? "***" : email.charAt(0) + "***" + email.substring(at);
    }
}
