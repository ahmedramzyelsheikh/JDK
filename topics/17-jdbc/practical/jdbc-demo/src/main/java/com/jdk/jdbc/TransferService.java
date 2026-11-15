package com.jdk.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.sql.DataSource;

/** Manual JDBC transactions: what Spring's @Transactional automates. */
public class TransferService {

    private final DataSource dataSource;

    public TransferService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void transfer(String from, String to, BigDecimal amount) {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);                                          // BEGIN
            try {
                adjust(c, from, amount.negate());
                if (balance(c, from).signum() < 0) {
                    throw new InsufficientFundsException(from + " cannot pay " + amount);
                }
                adjust(c, to, amount);
                c.commit();                                                  // COMMIT both updates together
            } catch (SQLException | RuntimeException e) {
                c.rollback();                                                // ROLLBACK both
                throw e;
            } finally {
                c.setAutoCommit(true);                                       // leave the pooled connection clean
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Savepoint: undo only part of a transaction (e.g. an optional bonus that failed). */
    public void transferWithOptionalBonus(String from, String to, BigDecimal amount, String bonusAccount) {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                adjust(c, from, amount.negate());
                adjust(c, to, amount);
                Savepoint beforeBonus = c.setSavepoint("bonus");
                try {
                    if (adjust(c, bonusAccount, BigDecimal.ONE) == 0) {
                        throw new SQLException("bonus account " + bonusAccount + " not found");
                    }
                } catch (SQLException bonusFailed) {
                    c.rollback(beforeBonus);                                 // keep the transfer, drop the bonus
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public BigDecimal balanceOf(String account) {
        try (Connection c = dataSource.getConnection()) {
            return balance(c, account);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int adjust(Connection c, String account, BigDecimal delta) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("update account set balance = balance + ? where id = ?")) {
            ps.setBigDecimal(1, delta);
            ps.setString(2, account);
            return ps.executeUpdate();
        }
    }

    private static BigDecimal balance(Connection c, String account) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("select balance from account where id = ?")) {
            ps.setString(1, account);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}
