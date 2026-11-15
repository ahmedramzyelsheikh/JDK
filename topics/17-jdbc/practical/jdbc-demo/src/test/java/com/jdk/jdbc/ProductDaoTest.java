package com.jdk.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jdk.jdbc.ProductDao.DuplicateOrInvalidDataException;
import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductDaoTest {

    HikariDataSource ds;
    ProductDao dao;

    @BeforeEach
    void setUp() {
        ds = Databases.pooled(5);
        dao = new ProductDao(ds);
    }

    @AfterEach
    void tearDown() {
        ds.close();
    }

    @Test
    void insertReturnsGeneratedIdAndRoundTripsAllTypes() {
        Product saved = dao.insert(new Product(null, "Laptop", new BigDecimal("999.90"), 5, LocalDate.of(2026, 3, 1)));

        assertThat(saved.id()).isPositive();
        assertThat(dao.findById(saved.id())).contains(saved);
    }

    @Test
    void nullColumnsStayNullNotZero() {
        Product saved = dao.insert(new Product(null, "Preorder", BigDecimal.TEN, null, null));

        Product loaded = dao.findById(saved.id()).orElseThrow();
        assertThat(loaded.stock()).isNull();                     // without wasNull() this would be 0
        assertThat(loaded.releasedOn()).isNull();
    }

    @Test
    void queryWithParameterAndOrdering() {
        dao.insert(new Product(null, "Mouse", new BigDecimal("25"), 10, null));
        dao.insert(new Product(null, "Keyboard", new BigDecimal("60"), 10, null));
        dao.insert(new Product(null, "Cable", new BigDecimal("5"), 10, null));

        assertThat(dao.findCheaperThan(new BigDecimal("50"))).extracting(Product::name).containsExactly("Cable", "Mouse");
    }

    @Test
    void updateAndDeleteReportAffectedRows() {
        Product p = dao.insert(new Product(null, "Lamp", new BigDecimal("30"), 1, null));

        assertThat(dao.updatePrice(p.id(), new BigDecimal("35"))).isEqualTo(1);
        assertThat(dao.updatePrice(9999, BigDecimal.ONE)).isZero();
        assertThat(dao.delete(p.id())).isTrue();
        assertThat(dao.findById(p.id())).isEmpty();
    }

    @Test
    void uniqueViolationIsTranslatedUsingSqlState() {
        dao.insert(new Product(null, "Desk", new BigDecimal("200"), 1, null));

        assertThatThrownBy(() -> dao.insert(new Product(null, "Desk", new BigDecimal("210"), 1, null)))
                .isInstanceOf(DuplicateOrInvalidDataException.class)
                .hasMessageContaining("SQLState 23");
    }
}
