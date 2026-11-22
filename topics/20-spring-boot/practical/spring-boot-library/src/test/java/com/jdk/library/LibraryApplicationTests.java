package com.jdk.library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jdk.library.config.LibraryProperties;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * The whole application on a random port: real HTTP, real Tomcat, real Flyway + Hibernate + H2.
 * Slower than slices, so keep a few of these for the critical paths.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "library.max-page-size=3")
@DirtiesContext                       // this test writes data: don't reuse its context (and DB) elsewhere
class LibraryApplicationTests {

    @Value("${local.server.port}")
    int port;

    @Autowired
    LibraryProperties properties;

    RestClient http;

    @BeforeEach
    void client() {
        http = RestClient.create("http://localhost:" + port);
    }

    @Test
    void configurationPropertiesAreBoundAndOverridable() {
        assertThat(properties.name()).isEqualTo("JDK Library");
        assertThat(properties.maxPageSize()).isEqualTo(3);             // overridden by the test's properties
    }

    @Test
    void createThenReadOverRealHttp() {
        ResponseEntity<Map<String, Object>> created = http.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Refactoring Databases", "isbn", "9780321293534", "authorId", 3, "price", 48))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() { });

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String location = created.getHeaders().getLocation().toString();

        Map<String, Object> fetched = http.get().uri(location).retrieve().body(new ParameterizedTypeReference<>() { });
        assertThat(fetched).containsEntry("title", "Refactoring Databases").containsEntry("author", "Martin Fowler");

        assertThatThrownBy(() -> http.post().uri("/api/books").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Dup", "isbn", "9780321293534", "authorId", 3, "price", 1))
                .retrieve().toBodilessEntity())
                .isInstanceOfSatisfying(HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void pageSizeIsCappedByConfiguration() {
        Map<String, Object> page = http.get().uri("/api/books?size=100").retrieve().body(new ParameterizedTypeReference<>() { });

        assertThat((List<?>) page.get("content")).hasSize(3);
        assertThat(page.get("size")).isEqualTo(3);
    }

    @Test
    void unknownSortPropertyIsAClientError() {
        assertThatThrownBy(() -> http.get().uri("/api/books?sort=password").retrieve().toBodilessEntity())
                .isInstanceOfSatisfying(HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void actuatorHealthIncludesTheDatabase() {
        Map<String, Object> health = http.get().uri("/actuator/health").retrieve().body(new ParameterizedTypeReference<>() { });

        assertThat(health).containsEntry("status", "UP");
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) health.get("components");
        assertThat(components).containsKey("db");
    }
}
