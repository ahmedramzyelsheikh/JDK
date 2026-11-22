package com.jdk.inventory;

import org.springframework.boot.SpringApplication;

/**
 * Run the REAL application locally with a throw-away PostgreSQL container - no database installation needed.
 * Run this main() from the IDE (needs Docker), or: ./mvnw -f topics/pom.xml -pl 23-integration-testing/practical/integration-tests-demo spring-boot:test-run
 */
public class TestInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.from(InventoryApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
