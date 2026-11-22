package com.jdk.inventory;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * A PostgreSQL container as a Spring bean. @ServiceConnection makes Spring Boot point spring.datasource.*
 * at the container automatically (no @DynamicPropertySource needed). Because the Spring test context is cached,
 * all test classes that import this configuration share ONE container.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }
}
