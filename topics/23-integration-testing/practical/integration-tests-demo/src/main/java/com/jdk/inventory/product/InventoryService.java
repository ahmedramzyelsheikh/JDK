package com.jdk.inventory.product;

import java.util.NoSuchElementException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    public record StockReserved(String sku, int quantity, int remaining) { }

    private final ProductRepository products;
    private final ApplicationEventPublisher events;

    public InventoryService(ProductRepository products, ApplicationEventPublisher events) {
        this.products = products;
        this.events = events;
    }

    @Transactional
    public Product create(String sku, String name, int stock) {
        return products.save(new Product(sku, name, stock));
    }

    @Transactional(readOnly = true)
    public Product get(String sku) {
        return products.findBySku(sku).orElseThrow(() -> new NoSuchElementException("unknown sku " + sku));
    }

    @Transactional
    public int reserve(String sku, int quantity) {
        Product product = products.findBySkuForUpdate(sku).orElseThrow(() -> new NoSuchElementException("unknown sku " + sku));
        product.reserve(quantity);                                              // dirty checking -> UPDATE at commit
        events.publishEvent(new StockReserved(sku, quantity, product.getStock()));  // delivered AFTER commit (see AuditListener)
        return product.getStock();
    }
}
