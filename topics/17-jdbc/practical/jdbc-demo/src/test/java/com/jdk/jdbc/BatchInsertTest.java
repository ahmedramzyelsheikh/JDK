package com.jdk.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchInsertTest {

    static final int ROWS = 2_000;
    HikariDataSource ds;

    @BeforeEach
    void setUp() {
        ds = Databases.pooled(2);
    }

    @AfterEach
    void tearDown() {
        ds.close();
    }

    @Test
    void batchInsertsAllRowsInOneTransaction() throws SQLException {
        long singleMs = time(() -> insertOneByOne("single-"));
        long batchMs = time(() -> insertAsBatch("batch-"));

        assertThat(count("single-%")).isEqualTo(ROWS);
        assertThat(count("batch-%")).isEqualTo(ROWS);
        System.out.printf("%d rows: one-by-one with auto-commit %d ms, batched in one transaction %d ms%n", ROWS, singleMs, batchMs);
        // against a real networked database the difference is usually 10-100x (one round trip per row vs per batch)
    }

    private void insertOneByOne(String prefix) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("insert into product (name, price) values (?, ?)")) {
            for (int i = 0; i < ROWS; i++) {
                ps.setString(1, prefix + i);
                ps.setBigDecimal(2, BigDecimal.ONE);
                ps.executeUpdate();                                  // one round trip + one commit per row
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void insertAsBatch(String prefix) {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("insert into product (name, price) values (?, ?)")) {
                for (int i = 0; i < ROWS; i++) {
                    ps.setString(1, prefix + i);
                    ps.setBigDecimal(2, BigDecimal.ONE);
                    ps.addBatch();
                    if ((i + 1) % 500 == 0) {
                        int[] counts = ps.executeBatch();            // send 500 rows at once
                        assertThat(Arrays.stream(counts).sum()).isEqualTo(500);
                    }
                }
                c.commit();
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private int count(String like) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("select count(*) from product where name like ?")) {
            ps.setString(1, like);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static long time(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return (System.nanoTime() - start) / 1_000_000;
    }
}
