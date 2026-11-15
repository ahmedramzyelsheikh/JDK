package com.jdk.linq;

import static org.assertj.core.api.Assertions.assertThat;

import com.jdk.linq.JooqQueries.CategoryRevenue;
import com.jdk.linq.JooqQueries.ProductLine;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.jooq.conf.ParamType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JooqQueriesTest {

    private Connection connection;
    private JooqQueries queries;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
        connection = DriverManager.getConnection("jdbc:h2:mem:test" + System.nanoTime());   // fresh DB per test
        queries = new JooqQueries(connection);
        queries.createAndFill();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void whereOrderBySelect() {
        assertThat(queries.expensiveProductNames(new BigDecimal("100")).fetch(JooqQueries.NAME))
                .containsExactly("Laptop", "Desk", "Monitor");
    }

    @Test
    void groupBySumOrderByTake() {
        assertThat(queries.topCategoriesByRevenue(2)).containsExactly(
                new CategoryRevenue("Tech", new BigDecimal("9520.00")),
                new CategoryRevenue("Home", new BigDecimal("2700.00")));
    }

    @Test
    void optionalFiltersAreComposedIntoOneWhereClause() {
        String sql = queries.search("Tech", new BigDecimal("20"), null).getSQL(ParamType.INLINED).toLowerCase();

        assertThat(sql).contains("\"category\" = 'tech'".toLowerCase()).contains("\"price\" >= 20").doesNotContain("<=");
        assertThat(queries.search(null, null, null).getSQL().toLowerCase()).doesNotContain("where");
    }

    @Test
    void joinMapsIntoRecords() {
        assertThat(queries.orderLines()).containsExactly(
                new ProductLine("Laptop", 1), new ProductLine("Mouse", 2),
                new ProductLine("Novel", 3), new ProductLine("Monitor", 1));
    }
}
