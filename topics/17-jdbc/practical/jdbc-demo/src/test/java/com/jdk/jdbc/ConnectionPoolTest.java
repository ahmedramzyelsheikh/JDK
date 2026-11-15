package com.jdk.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConnectionPoolTest {

    HikariDataSource ds;

    @BeforeEach
    void setUp() {
        ds = Databases.pooled(2);                  // max 2 connections, 500 ms connection timeout
    }

    @AfterEach
    void tearDown() {
        ds.close();
    }

    @Test
    void closingAPooledConnectionReturnsItForReuse() throws SQLException {
        Set<String> physical = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            try (Connection c = ds.getConnection()) {                 // borrow
                physical.add(c.unwrap(Connection.class).toString()); // the real driver connection behind the proxy
            }                                                          // "close" = give back
        }
        assertThat(physical).hasSizeLessThanOrEqualTo(2);              // 20 borrows, at most 2 physical connections
    }

    @Test
    void exhaustedPoolMakesCallersWaitThenFail() throws SQLException {
        HikariPoolMXBean pool = ds.getHikariPoolMXBean();
        try (Connection a = ds.getConnection(); Connection b = ds.getConnection()) {
            assertThat(pool.getActiveConnections()).isEqualTo(2);
            long start = System.nanoTime();

            assertThatThrownBy(ds::getConnection)                      // a 3rd borrower waits 500 ms, then gives up
                    .isInstanceOf(SQLTransientConnectionException.class)
                    .hasMessageContaining("Connection is not available");

            assertThat((System.nanoTime() - start) / 1_000_000).isGreaterThanOrEqualTo(450);
        }
        assertThat(pool.getActiveConnections()).isZero();
    }
}
