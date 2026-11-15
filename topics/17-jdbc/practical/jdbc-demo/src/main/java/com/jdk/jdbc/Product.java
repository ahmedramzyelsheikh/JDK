package com.jdk.jdbc;

import java.math.BigDecimal;
import java.time.LocalDate;

/** stock and releasedOn can be NULL in the database, so they are wrapper / nullable types. */
public record Product(Long id, String name, BigDecimal price, Integer stock, LocalDate releasedOn) {

    public Product withId(long newId) {
        return new Product(newId, name, price, stock, releasedOn);
    }
}
