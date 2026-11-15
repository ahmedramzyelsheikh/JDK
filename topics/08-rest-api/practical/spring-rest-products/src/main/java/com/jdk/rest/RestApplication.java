package com.jdk.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Run: ./mvnw -f topics/pom.xml -pl 08-rest-api/practical/spring-rest-products spring-boot:run
 * Then try the requests in requests.http.
 */
@SpringBootApplication
public class RestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }
}
