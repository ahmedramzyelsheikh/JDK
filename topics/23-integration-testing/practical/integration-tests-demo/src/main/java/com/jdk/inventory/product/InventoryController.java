package com.jdk.inventory.product;

import com.jdk.inventory.product.Product.InsufficientStockException;
import java.net.URI;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class InventoryController {

    public record CreateProduct(String sku, String name, int stock) { }
    public record ProductView(String sku, String name, int stock) { }
    public record Reservation(int quantity) { }

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductView> create(@RequestBody CreateProduct request) {
        Product p = service.create(request.sku(), request.name(), request.stock());
        return ResponseEntity.created(URI.create("/api/products/" + p.getSku())).body(new ProductView(p.getSku(), p.getName(), p.getStock()));
    }

    @GetMapping("/{sku}")
    public ProductView get(@PathVariable String sku) {
        Product p = service.get(sku);
        return new ProductView(p.getSku(), p.getName(), p.getStock());
    }

    @PostMapping("/{sku}/reservations")
    public ResponseEntity<Map<String, Integer>> reserve(@PathVariable String sku, @RequestBody Reservation reservation) {
        int remaining = service.reserve(sku, reservation.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("remaining", remaining));
    }

    @ExceptionHandler(InsufficientStockException.class)
    ProblemDetail insufficient(InsufficientStockException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail notFound(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
