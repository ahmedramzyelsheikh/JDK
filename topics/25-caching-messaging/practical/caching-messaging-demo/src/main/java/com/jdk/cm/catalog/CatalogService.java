package com.jdk.cm.catalog;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cache-aside with Spring's cache abstraction: the annotations are applied by a proxy (topic 19), the cache
 * provider (Caffeine, Redis...) is configuration.
 */
@Service
public class CatalogService {

    public record Product(String sku, String name, BigDecimal price) { }

    private final Map<String, Product> database = new ConcurrentHashMap<>(Map.of(
            "PEN", new Product("PEN", "Pen", new BigDecimal("2.50")),
            "MUG", new Product("MUG", "Mug", new BigDecimal("7.00"))));
    private final AtomicInteger databaseReads = new AtomicInteger();

    /** First call reads the "database" (slow); later calls with the same sku are served from the cache. */
    @Cacheable(cacheNames = "products", key = "#sku")
    public Product find(String sku) {
        databaseReads.incrementAndGet();
        sleep(50);                                                    // pretend this is a slow query / remote call
        Product p = database.get(sku);
        if (p == null) {
            throw new IllegalArgumentException("unknown sku " + sku);  // exceptions are NOT cached
        }
        return p;
    }

    /** Write-through for this key: update the source AND put the new value into the cache. */
    @CachePut(cacheNames = "products", key = "#product.sku()")
    public Product update(Product product) {
        database.put(product.sku(), product);
        return product;
    }

    /** Invalidate: the next read goes to the database again. */
    @CacheEvict(cacheNames = "products", key = "#sku")
    public void delete(String sku) {
        database.remove(sku);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public void clearCache() {
    }

    public int databaseReads() {
        return databaseReads.get();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
