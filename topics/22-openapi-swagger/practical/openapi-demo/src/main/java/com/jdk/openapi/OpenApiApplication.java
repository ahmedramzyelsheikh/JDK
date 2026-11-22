package com.jdk.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Run: ./mvnw -f topics/pom.xml -pl 22-openapi-swagger/practical/openapi-demo spring-boot:run
 * Open http://localhost:8080/swagger-ui.html  (spec: http://localhost:8080/v3/api-docs  or  /v3/api-docs.yaml)
 */
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(title = "Task API", version = "1.0.0",
                description = "Example API of topic 22: documented from code with springdoc-openapi.",
                contact = @Contact(name = "JDK course"),
                license = @License(name = "MIT")),
        servers = @Server(url = "http://localhost:8080", description = "local"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiApplication.class, args);
    }
}
