package com.jdk.library.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * JPA slice: only entities, repositories, DataSource, Flyway and Hibernate start. Each test runs in a
 * transaction that is ROLLED BACK at the end, so tests don't see each other's changes.
 */
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    BookRepository books;

    @Autowired
    AuthorRepository authors;

    @Test
    void flywaySeedDataIsThere() {
        assertThat(books.count()).isEqualTo(5);
        assertThat(authors.count()).isEqualTo(3);
    }

    @Test
    void derivedQueryWithPagingAndSorting() {
        Page<Book> page = books.findByTitleContainingIgnoreCase("clean", PageRequest.of(0, 1, Sort.by("title")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Book::getTitle).containsExactly("Clean Architecture");
        assertThat(page.getContent().getFirst().getAuthor().getName()).isEqualTo("Robert C. Martin");   // fetched via entity graph
    }

    @Test
    void derivedQueriesThroughAnAssociation() {
        List<Book> bloch = books.findByAuthorNameOrderByPublishedOnDesc("Joshua Bloch");

        assertThat(bloch).extracting(Book::getTitle).containsExactly("Effective Java", "Java Puzzlers");
        assertThat(books.countByPriceGreaterThan(new BigDecimal("40"))).isEqualTo(2);
        assertThat(books.existsByIsbn("9780132350884")).isTrue();
    }

    @Test
    void constructorExpressionProjection() {
        assertThat(authors.statistics()).containsExactly(
                new AuthorStats("Joshua Bloch", 2, 37.5),
                new AuthorStats("Robert C. Martin", 2, 37.5),
                new AuthorStats("Martin Fowler", 1, 50.0));
    }

    @Test
    void saveAssignsIdAndVersion() {
        Author fowler = authors.findAll().stream().filter(a -> a.getName().equals("Martin Fowler")).findFirst().orElseThrow();

        Book saved = books.saveAndFlush(new Book("PoEAA", "9780321127426", new BigDecimal("55"), null, fowler));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isZero();
    }
}
