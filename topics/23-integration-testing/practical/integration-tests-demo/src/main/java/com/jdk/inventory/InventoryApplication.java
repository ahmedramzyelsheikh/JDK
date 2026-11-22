package com.jdk.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Needs PostgreSQL. For local development without installing it, run TestInventoryApplication
 * (src/test/java): it starts the app with a PostgreSQL container from Testcontainers.
 */
@SpringBootApplication
@EnableAsync
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
