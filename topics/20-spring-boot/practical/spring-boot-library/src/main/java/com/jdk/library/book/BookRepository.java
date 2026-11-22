package com.jdk.library.book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    /** Derived query (parsed from the method name) + paging + entity graph (author fetched in the same SQL). */
    @EntityGraph(attributePaths = "author")
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Optional<Book> findWithAuthorById(Long id);

    boolean existsByIsbn(String isbn);

    List<Book> findByAuthorNameOrderByPublishedOnDesc(String authorName);

    long countByPriceGreaterThan(BigDecimal price);
}
