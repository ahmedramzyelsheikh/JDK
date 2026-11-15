package com.jdk.orm;

import static org.assertj.core.api.Assertions.assertThat;

import com.jdk.orm.activerecord.ActiveRecordQueries;
import com.jdk.orm.jdbc.JdbcBookGateway;
import com.jdk.orm.jdbi.JdbiBookQueries;
import com.jdk.orm.jpa.Jpa;
import com.jdk.orm.jpa.JpaBookQueries;
import com.jdk.orm.mybatis.MyBatisBookQueries;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** The SAME behaviour, verified against every data-access style, each on its own fresh database. */
class BookQueriesContractTest {

    static Stream<Arguments> styles() {
        return Stream.of(
                style("JDBC", () -> new JdbcBookGateway(Db.freshDatabase(uniqueName()))),
                style("Active Record", () -> new ActiveRecordQueries(Db.freshDatabase(uniqueName()))),
                style("MyBatis", () -> new MyBatisBookQueries(Db.freshDatabase(uniqueName()))),
                style("JDBI", () -> new JdbiBookQueries(Db.freshDatabase(uniqueName()))),
                style("JPA/Hibernate", () -> {
                    EntityManagerFactory emf = Jpa.create(uniqueName(), false);
                    Jpa.seed(emf);
                    return new JpaBookQueries(emf);
                }));
    }

    private static Arguments style(String name, Supplier<BookQueries> factory) {
        return Arguments.of(Named.of(name, factory));
    }

    private static String uniqueName() {
        return "db" + UUID.randomUUID().toString().replace("-", "");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("styles")
    void findsBooksPricierThanOrderedByPriceDesc(Supplier<BookQueries> factory) {
        List<BookView> books = factory.get().booksPricierThan(new BigDecimal("39"));

        assertThat(books).extracting(BookView::title).containsExactly(
                "Patterns of Enterprise Application Architecture", "Refactoring", "Effective Java", "Clean Code");
        assertThat(books.getFirst().author()).isEqualTo("Martin Fowler");
        assertThat(books.getFirst().price()).isEqualByComparingTo("55");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("styles")
    void insertedBookIsVisibleInQueries(Supplier<BookQueries> factory) {
        BookQueries queries = factory.get();

        long id = queries.addBook("Domain-Driven Design", new BigDecimal("60.00"), 3);

        assertThat(id).isPositive();
        assertThat(queries.booksPricierThan(new BigDecimal("59")))
                .singleElement()
                .satisfies(b -> {
                    assertThat(b.title()).isEqualTo("Domain-Driven Design");
                    assertThat(b.author()).isEqualTo("Martin Fowler");
                });
    }
}
