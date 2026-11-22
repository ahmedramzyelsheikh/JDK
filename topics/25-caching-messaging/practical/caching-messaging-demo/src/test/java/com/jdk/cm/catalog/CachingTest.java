package com.jdk.cm.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.jdk.cm.catalog.CatalogService.Product;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/** A tiny Spring context with only the cache: no Redis or Kafka needed for these tests. */
@SpringJUnitConfig(CachingTest.CacheConfig.class)
@DirtiesContext
class CachingTest {

    @Configuration
    @EnableCaching
    @Import(CatalogService.class)
    static class CacheConfig {
        @Bean
        CacheManager cacheManager() {
            CaffeineCacheManager manager = new CaffeineCacheManager("products");
            manager.setCaffeine(Caffeine.newBuilder().maximumSize(100).expireAfterWrite(Duration.ofMinutes(10)).recordStats());
            return manager;
        }
    }

    @Autowired
    CatalogService catalog;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void clear() {
        catalog.clearCache();
    }

    @Test
    void secondReadIsServedFromTheCache() {
        int before = catalog.databaseReads();
        long start = System.nanoTime();
        catalog.find("PEN");
        long firstMs = (System.nanoTime() - start) / 1_000_000;
        start = System.nanoTime();
        catalog.find("PEN");
        long secondMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(catalog.databaseReads() - before).isEqualTo(1);           // only ONE database read
        assertThat(firstMs).isGreaterThanOrEqualTo(45);
        assertThat(secondMs).isLessThan(firstMs);
        assertThat(cacheManager.getCache("products").get("PEN")).isNotNull();
    }

    @Test
    void cachePutKeepsTheCacheFreshOnUpdate() {
        catalog.find("MUG");
        catalog.update(new Product("MUG", "Mug XL", new BigDecimal("9.00")));
        int reads = catalog.databaseReads();

        assertThat(catalog.find("MUG").name()).isEqualTo("Mug XL");           // new value, no database read
        assertThat(catalog.databaseReads()).isEqualTo(reads);
    }

    @Test
    void evictForcesTheNextReadToTheDatabase() {
        catalog.find("PEN");
        catalog.delete("PEN");

        assertThatThrownBy(() -> catalog.find("PEN")).hasMessage("unknown sku PEN");   // cache didn't serve stale data
    }
}
