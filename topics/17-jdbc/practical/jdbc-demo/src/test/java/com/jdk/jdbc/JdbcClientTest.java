package com.jdk.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** The same data access with Spring's JdbcClient: no boilerplate, translated exceptions. */
class JdbcClientTest {

    HikariDataSource ds;
    JdbcClient jdbc;

    record ProductRow(Long id, String name, BigDecimal price, Integer stock) { }

    @BeforeEach
    void setUp() {
        ds = Databases.pooled(3);
        jdbc = JdbcClient.create(ds);
        for (String[] p : new String[][] {{"Mouse", "25"}, {"Keyboard", "60"}, {"Cable", "5"}}) {
            jdbc.sql("insert into product (name, price, stock) values (:name, :price, 10)")
                    .param("name", p[0]).param("price", new BigDecimal(p[1])).update();
        }
    }

    @AfterEach
    void tearDown() {
        ds.close();
    }

    @Test
    void queryMapsRowsToRecords() {
        List<ProductRow> cheap = jdbc.sql("select id, name, price, stock from product where price < :max order by price")
                .param("max", new BigDecimal("50"))
                .query(ProductRow.class)
                .list();

        assertThat(cheap).extracting(ProductRow::name).containsExactly("Cable", "Mouse");
    }

    @Test
    void singleValuesAndOptionals() {
        Integer count = jdbc.sql("select count(*) from product").query(Integer.class).single();
        Optional<String> missing = jdbc.sql("select name from product where id = :id").param("id", 999).query(String.class).optional();

        assertThat(count).isEqualTo(3);
        assertThat(missing).isEmpty();
    }

    @Test
    void generatedKeysAndUpdates() {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.sql("insert into product (name, price) values (:n, :p)").param("n", "Webcam").param("p", BigDecimal.TEN)
                .update(keys, "id");

        int updated = jdbc.sql("update product set price = price * 2 where id = :id").param("id", keys.getKey()).update();

        assertThat(updated).isEqualTo(1);
        assertThat(jdbc.sql("select price from product where name = 'Webcam'").query(BigDecimal.class).single())
                .isEqualByComparingTo("20");
    }

    @Test
    void sqlExceptionsAreTranslatedToSpringsHierarchy() {
        assertThatThrownBy(() -> jdbc.sql("insert into product (name, price) values ('Mouse', 1)").update())
                .isInstanceOf(DuplicateKeyException.class);              // unchecked, database-independent
    }
}
