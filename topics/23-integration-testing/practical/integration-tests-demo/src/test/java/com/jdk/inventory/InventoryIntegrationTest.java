package com.jdk.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.jdk.inventory.product.InventoryService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full application + real PostgreSQL in Docker + real HTTP. Skipped automatically when Docker is not available.
 * Tests use unique SKUs because the context (and the database) is shared between test methods and classes.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class InventoryIntegrationTest {

    @Value("${local.server.port}")
    int port;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    InventoryService inventory;

    RestClient http;

    @BeforeEach
    void client() {
        http = RestClient.create("http://localhost:" + port);
    }

    @Test
    void runsAgainstRealPostgresWithFlywayMigrations() {
        String version = jdbc.sql("select version()").query(String.class).single();
        Integer migrations = jdbc.sql("select count(*) from flyway_schema_history where success").query(Integer.class).single();

        assertThat(version).startsWith("PostgreSQL 17");
        assertThat(migrations).isPositive();
    }

    @Test
    void createAndReserveOverHttp() {
        http.post().uri("/api/products").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("sku", "HTTP-1", "name", "Keyboard", "stock", 5)).retrieve().toBodilessEntity();

        Map<String, Integer> result = http.post().uri("/api/products/HTTP-1/reservations").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("quantity", 2)).retrieve().body(new ParameterizedTypeReference<>() { });

        assertThat(result).containsEntry("remaining", 3);
        assertThatThrownBy(() -> http.post().uri("/api/products/HTTP-1/reservations").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("quantity", 99)).retrieve().toBodilessEntity())
                .isInstanceOfSatisfying(HttpClientErrorException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void concurrentReservationsNeverOversell() throws Exception {
        inventory.create("RACE-1", "Last items", 5);
        List<Callable<Boolean>> buyers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            buyers.add(() -> {
                try {
                    inventory.reserve("RACE-1", 1);
                    return true;
                } catch (RuntimeException soldOut) {
                    return false;
                }
            });
        }

        int successes = 0;
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            for (Future<Boolean> f : pool.invokeAll(buyers)) {
                successes += f.get() ? 1 : 0;
            }
        }

        assertThat(successes).isEqualTo(5);                                  // SELECT ... FOR UPDATE serialised them
        assertThat(inventory.get("RACE-1").getStock()).isZero();
    }

    @Test
    void auditRowIsWrittenAsynchronouslyAfterCommitAsJsonb() {
        inventory.create("AUDIT-1", "Mouse", 10);

        inventory.reserve("AUDIT-1", 4);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->           // no Thread.sleep: poll until true
                assertThat(jdbc.sql("select details->>'remaining' from audit_log where details->>'sku' = 'AUDIT-1'")
                        .query(String.class).list()).containsExactly("6"));
    }

    @Test
    @Sql("/sql/bulk-products.sql")                                        // test data loaded before this test
    void sqlAnnotationLoadsTestData() {
        assertThat(jdbc.sql("select count(*) from product where sku like 'BULK-%' and stock > 0").query(Integer.class).single())
                .isEqualTo(2);
    }
}
