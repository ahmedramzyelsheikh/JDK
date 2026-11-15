package com.jdk.orm;

import java.math.BigDecimal;

/** Read model returned by every style: a DTO, not an entity. */
public record BookView(String title, String author, BigDecimal price) { }
