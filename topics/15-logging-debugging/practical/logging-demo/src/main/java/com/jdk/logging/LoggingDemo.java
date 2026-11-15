package com.jdk.logging;

import com.jdk.logging.OrderProcessor.Order;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run: ./mvnw -q -f topics/pom.xml -pl 15-logging-debugging/practical/logging-demo compile exec:java
 * Console shows the pattern layout; target/logs/app.json.log contains the same events as JSON.
 */
public class LoggingDemo {

    private static final Logger log = LoggerFactory.getLogger(LoggingDemo.class);

    public static void main(String[] args) throws IOException {
        log.trace("TRACE is below DEBUG: not printed for com.jdk.logging");
        log.debug("DEBUG enabled for com.jdk.logging (see logback.xml)");
        LoggerFactory.getLogger("org.example.library").debug("DEBUG from another package: NOT printed (root is INFO)");

        OrderProcessor processor = new OrderProcessor();
        processor.handle(new Order(1, "ali@example.com", "4111111111111111", new BigDecimal("99.90")));
        processor.handle(new Order(2, "sara@example.com", "5500000000000004", new BigDecimal("25000")));
        processor.handle(new Order(3, "omar@example.com", "0000123412341234", new BigDecimal("10")));

        Path json = Path.of("target/logs/app.json.log");
        if (Files.exists(json)) {
            System.out.println("\nFirst JSON log line from " + json + ":");
            System.out.println(Files.readAllLines(json).getFirst());
        }
    }
}
