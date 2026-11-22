package com.jdk.library.book;

/** Read model filled directly by a JPQL constructor expression (no entities loaded). */
public record AuthorStats(String author, long books, double averagePrice) { }
