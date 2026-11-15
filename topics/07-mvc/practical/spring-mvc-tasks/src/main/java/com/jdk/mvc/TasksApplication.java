package com.jdk.mvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan.
 * Run: ./mvnw -f topics/pom.xml -pl 07-mvc/practical/spring-mvc-tasks spring-boot:run  -> http://localhost:8080
 */
@SpringBootApplication
public class TasksApplication {

    public static void main(String[] args) {
        SpringApplication.run(TasksApplication.class, args);
    }
}
