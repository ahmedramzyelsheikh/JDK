package com.jdk.rest.product;

import java.math.BigDecimal;

/**
 * Domain model (never sent to clients directly). {@code version} increases on every change and is
 * exposed as the HTTP ETag for optimistic concurrency.
 */
public record Product(long id, String name, String category, BigDecimal price, int stock, long version) {

    Product withId(long newId) {
        return new Product(newId, name, category, price, stock, version);
    }

    Product nextVersion(String newName, String newCategory, BigDecimal newPrice, int newStock) {
        return new Product(id, newName, newCategory, newPrice, newStock, version + 1);
    }
}
