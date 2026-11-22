package com.jdk.inventory.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    private String name;

    private int stock;

    protected Product() {
    }

    public Product(String sku, String name, int stock) {
        this.sku = sku;
        this.name = name;
        this.stock = stock;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (quantity > stock) {
            throw new InsufficientStockException(sku, stock, quantity);
        }
        stock -= quantity;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public int getStock() { return stock; }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String sku, int available, int requested) {
            super("only " + available + " of " + sku + " left, requested " + requested);
        }
    }
}
