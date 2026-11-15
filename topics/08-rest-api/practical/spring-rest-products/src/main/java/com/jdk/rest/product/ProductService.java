package com.jdk.rest.product;

import com.jdk.rest.error.ApiExceptions.ConflictException;
import com.jdk.rest.error.ApiExceptions.NotFoundException;
import com.jdk.rest.error.ApiExceptions.PreconditionFailedException;
import com.jdk.rest.product.ProductDtos.PageResponse;
import com.jdk.rest.product.ProductDtos.ProductPatch;
import com.jdk.rest.product.ProductDtos.ProductRequest;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Business rules: unique names, optimistic concurrency, filtering, sorting and paging. */
@Service
public class ProductService {

    private static final Map<String, Comparator<Product>> SORTABLE = Map.of(
            "id", Comparator.comparingLong(Product::id),
            "name", Comparator.comparing(Product::name, String.CASE_INSENSITIVE_ORDER),
            "price", Comparator.comparing(Product::price),
            "stock", Comparator.comparingInt(Product::stock));

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(ProductRequest req) {
        requireUniqueName(req.name(), 0);
        return repository.insert(new Product(0, req.name().strip(), req.category(), req.price(), req.stockOrZero(), 1));
    }

    public Product get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("product", id));
    }

    public PageResponse<Product> list(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                      String sort, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be >= 0 and size between 1 and 100");
        }
        String[] sortParts = sort.split(",");
        Comparator<Product> comparator = SORTABLE.get(sortParts[0]);
        if (comparator == null) {
            throw new IllegalArgumentException("cannot sort by '" + sortParts[0] + "', use one of " + SORTABLE.keySet());
        }
        if (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        List<Product> filtered = repository.findAll().stream()
                .filter(p -> category == null || p.category().equalsIgnoreCase(category))
                .filter(p -> minPrice == null || p.price().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.price().compareTo(maxPrice) <= 0)
                .sorted(comparator)
                .toList();
        List<Product> content = filtered.stream().skip((long) page * size).limit(size).toList();
        int totalPages = (int) Math.ceil(filtered.size() / (double) size);
        return new PageResponse<>(content, page, size, filtered.size(), totalPages);
    }

    /** PUT: full replacement. expectedVersion comes from the If-Match header (may be null = no check). */
    public Product replace(long id, ProductRequest req, Long expectedVersion) {
        Product current = get(id);
        checkVersion(current, expectedVersion);
        requireUniqueName(req.name(), id);
        return repository.update(current.nextVersion(req.name().strip(), req.category(), req.price(), req.stockOrZero()));
    }

    /** PATCH: only the provided fields change. */
    public Product patch(long id, ProductPatch patch, Long expectedVersion) {
        Product current = get(id);
        checkVersion(current, expectedVersion);
        if (patch.name() != null) {
            requireUniqueName(patch.name(), id);
        }
        return repository.update(current.nextVersion(
                patch.name() != null ? patch.name().strip() : current.name(),
                patch.category() != null ? patch.category() : current.category(),
                patch.price() != null ? patch.price() : current.price(),
                patch.stock() != null ? patch.stock() : current.stock()));
    }

    public void delete(long id) {
        get(id);
        repository.delete(id);
    }

    private void requireUniqueName(String name, long excludingId) {
        if (repository.existsByNameIgnoreCase(name, excludingId)) {
            throw new ConflictException("a product named '" + name.strip() + "' already exists");
        }
    }

    private static void checkVersion(Product current, Long expectedVersion) {
        if (expectedVersion != null && expectedVersion != current.version()) {
            throw new PreconditionFailedException(
                    "product " + current.id() + " was modified (version " + current.version() + "), reload and retry");
        }
    }
}
