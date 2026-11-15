package com.jdk.rest.product;

import com.jdk.rest.product.ProductDtos.PageResponse;
import com.jdk.rest.product.ProductDtos.ProductPatch;
import com.jdk.rest.product.ProductDtos.ProductRequest;
import com.jdk.rest.product.ProductDtos.ProductResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller: resources, HTTP methods, status codes and headers. No business logic here.
 *
 * GET    /api/products?category=&minPrice=&maxPrice=&sort=price,desc&page=0&size=10   200
 * GET    /api/products/{id}                                                         200 + ETag | 404
 * POST   /api/products                                                              201 + Location + ETag | 400 | 409
 * PUT    /api/products/{id}   (If-Match optional)                                   200 + ETag | 400 | 404 | 409 | 412
 * PATCH  /api/products/{id}   (If-Match optional)                                   200 + ETag | 400 | 404 | 409 | 412
 * DELETE /api/products/{id}                                                         204 | 404
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ProductResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<Product> result = service.list(category, minPrice, maxPrice, sort, page, size);
        return new PageResponse<>(result.content().stream().map(ProductResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable long id) {
        Product p = service.get(id);
        return ResponseEntity.ok().eTag(etag(p)).body(ProductResponse.from(p));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product p = service.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + p.id()))    // 201 + Location header
                .eTag(etag(p))
                .body(ProductResponse.from(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> replace(@PathVariable long id,
                                                   @Valid @RequestBody ProductRequest request,
                                                   @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        Product p = service.replace(id, request, version(ifMatch));
        return ResponseEntity.ok().eTag(etag(p)).body(ProductResponse.from(p));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> patch(@PathVariable long id,
                                                 @Valid @RequestBody ProductPatch patch,
                                                 @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        Product p = service.patch(id, patch, version(ifMatch));
        return ResponseEntity.ok().eTag(etag(p)).body(ProductResponse.from(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();                                 // 204
    }

    private static String etag(Product p) {
        return "\"" + p.version() + "\"";
    }

    private static Long version(String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) {
            return null;
        }
        String raw = ifMatch.replace("W/", "").replace("\"", "").strip();
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("If-Match must be an ETag like \"3\"");
        }
    }
}
