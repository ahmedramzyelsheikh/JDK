package com.jdk.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Run: ./mvnw -f topics/pom.xml -pl 21-spring-security/practical/spring-security-jwt spring-boot:run
 *   TOKEN=$(curl -s -u ali:ali-password -X POST localhost:8080/api/auth/token | jq -r .accessToken)
 *   curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/me
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }
}
