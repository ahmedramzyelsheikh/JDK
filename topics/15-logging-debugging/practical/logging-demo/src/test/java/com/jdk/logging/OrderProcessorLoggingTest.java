package com.jdk.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jdk.logging.OrderProcessor.Order;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/** Captures log events in memory with Logback's ListAppender and asserts on them. */
class OrderProcessorLoggingTest {

    private final OrderProcessor processor = new OrderProcessor();
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(OrderProcessor.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
        logger.setLevel(null);                      // back to the configured level
    }

    private List<String> messages() {
        return appender.list.stream().map(e -> e.getLevel() + " " + e.getFormattedMessage()).toList();
    }

    @Test
    void successfulOrderLogsInfoWithMaskedData() {
        processor.handle(new Order(1, "ali@example.com", "4111111111111111", new BigDecimal("99.90")));

        assertThat(messages()).contains(
                "INFO Processing order 1 for a***@example.com amount 99.90",
                "DEBUG Paying with card **** **** **** 1111",
                "INFO Order paid");
        assertThat(String.join("\n", messages())).doesNotContain("4111111111111111");   // no card number leak
    }

    @Test
    void everyEventOfARequestHasTheSameRequestIdAndMdcIsClearedAfterwards() {
        processor.handle(new Order(2, "sara@example.com", "5500000000000004", BigDecimal.TEN));

        List<String> ids = appender.list.stream().map(e -> e.getMDCPropertyMap().get("requestId")).distinct().toList();
        assertThat(ids).hasSize(1).doesNotContainNull();
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void declinedPaymentIsAWarningWithoutStackTrace() {
        processor.handle(new Order(3, "omar@example.com", "4111111111111111", new BigDecimal("25000")));

        ILoggingEvent warn = appender.list.stream().filter(e -> e.getLevel() == Level.WARN).findFirst().orElseThrow();
        assertThat(warn.getFormattedMessage()).isEqualTo("Payment declined for order 3: amount above card limit");
        assertThat(warn.getThrowableProxy()).isNull();
    }

    @Test
    void unexpectedFailureIsAnErrorWithTheException() {
        processor.handle(new Order(4, "x@example.com", "0000123412341234", BigDecimal.ONE));

        ILoggingEvent error = appender.list.stream().filter(e -> e.getLevel() == Level.ERROR).findFirst().orElseThrow();
        assertThat(error.getThrowableProxy().getClassName()).isEqualTo(IllegalStateException.class.getName());
    }

    @Test
    void raisingTheLevelHidesDebugAndInfo() {
        logger.setLevel(Level.WARN);

        processor.handle(new Order(5, "ali@example.com", "4111111111111111", BigDecimal.ONE));

        assertThat(appender.list).isEmpty();
    }

    @Test
    void keyValuePairsAreAttachedToTheEvent() {
        processor.handle(new Order(6, "ali@example.com", "4111111111111111", new BigDecimal("5.00")));

        ILoggingEvent paid = appender.list.stream().filter(e -> e.getFormattedMessage().equals("Order paid")).findFirst().orElseThrow();
        assertThat(paid.getKeyValuePairs()).extracting(kv -> kv.key + "=" + kv.value).containsExactly("orderId=6", "amount=5.00");
    }
}
