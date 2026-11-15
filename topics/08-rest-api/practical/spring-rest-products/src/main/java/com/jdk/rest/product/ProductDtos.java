package com.jdk.rest.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs = the API contract. Requests and responses are separate records, so clients can never set
 * fields like id or version, and internal fields never leak out.
 */
public final class ProductDtos {

    private ProductDtos() {
    }

    /**
     * POST /api/products and PUT /api/products/{id} (full replacement).
     * stock is an Integer, not an int: Jackson 3 fails when a JSON field for a primitive is missing
     * (FAIL_ON_NULL_FOR_PRIMITIVES is on by default), so optional numbers should be wrapper types.
     */
    public record ProductRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank String category,
            @NotNull @Positive BigDecimal price,
            @PositiveOrZero Integer stock) {

        public int stockOrZero() {
            return stock == null ? 0 : stock;
        }
    }

    /** PATCH /api/products/{id}: every field optional; only non-null fields change. */
    public record ProductPatch(
            @Size(min = 1, max = 80) String name,
            String category,
            @Positive BigDecimal price,
            @PositiveOrZero Integer stock) { }

    public record ProductResponse(long id, String name, String category, BigDecimal price, int stock) {
        static ProductResponse from(Product p) {
            return new ProductResponse(p.id(), p.name(), p.category(), p.price(), p.stock());
        }
    }

    /** Generic page wrapper returned by list endpoints. */
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
}
