package com.jdk.orm;

import java.math.BigDecimal;
import java.util.List;

/** The use case every data-access style implements, so they can be compared side by side. */
public interface BookQueries {

    /** Books costing more than {@code min}, with author name, most expensive first. */
    List<BookView> booksPricierThan(BigDecimal min);

    /** Inserts a book and returns its generated id. */
    long addBook(String title, BigDecimal price, long authorId);

    String style();
}
