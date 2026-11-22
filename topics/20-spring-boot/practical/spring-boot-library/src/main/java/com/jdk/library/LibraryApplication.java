package com.jdk.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * @SpringBootApplication = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan(this package and below).
 * Run: ./mvnw -f topics/pom.xml -pl 20-spring-boot/practical/spring-boot-library spring-boot:run
 *      then: curl localhost:8080/api/books   |   curl localhost:8080/actuator/health
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
