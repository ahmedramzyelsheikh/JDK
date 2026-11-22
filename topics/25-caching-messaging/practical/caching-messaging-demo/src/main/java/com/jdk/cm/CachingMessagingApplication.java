package com.jdk.cm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/** Needs Redis and Kafka (e.g. docker compose, see README). The tests start both with Testcontainers. */
@SpringBootApplication
@EnableCaching
public class CachingMessagingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CachingMessagingApplication.class, args);
    }
}
