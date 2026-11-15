package com.jdk.rest.product;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/** In-memory repository (swap for Spring Data JPA in topic 10/20). */
@Repository
public class ProductRepository {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public Product insert(Product product) {
        Product saved = product.withId(ids.incrementAndGet());
        store.put(saved.id(), saved);
        return saved;
    }

    public Product update(Product product) {
        store.put(product.id(), product);
        return product;
    }

    public Optional<Product> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Collection<Product> findAll() {
        return store.values();
    }

    public boolean existsByNameIgnoreCase(String name, long excludingId) {
        return store.values().stream().anyMatch(p -> p.id() != excludingId && p.name().equalsIgnoreCase(name.strip()));
    }

    public void delete(long id) {
        store.remove(id);
    }

    public void deleteAll() {
        store.clear();
        ids.set(0);
    }
}
