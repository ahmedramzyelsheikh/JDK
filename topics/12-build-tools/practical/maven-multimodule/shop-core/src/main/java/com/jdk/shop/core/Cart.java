package com.jdk.shop.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/** Library code shared by other modules. */
public class Cart {

    public record Line(String product, BigDecimal unitPrice, int quantity) {
        public BigDecimal total() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    private final List<Line> lines = new ArrayList<>();

    public Cart add(String product, String unitPrice, int quantity) {
        if (StringUtils.isBlank(product) || quantity <= 0) {           // third-party dependency in use
            throw new IllegalArgumentException("product and a positive quantity are required");
        }
        lines.add(new Line(StringUtils.capitalize(product.strip()), new BigDecimal(unitPrice), quantity));
        return this;
    }

    public BigDecimal total() {
        return lines.stream().map(Line::total).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    public List<Line> lines() {
        return List.copyOf(lines);
    }
}
