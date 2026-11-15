package com.jdk.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** Plain-JDBC data access object: every resource in try-with-resources, every value bound with '?'. */
public class ProductDao {

    private final DataSource dataSource;

    public ProductDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Product insert(Product p) {
        String sql = "insert into product (name, price, stock, released_on) values (?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.name());
            ps.setBigDecimal(2, p.price());
            if (p.stock() == null) {
                ps.setNull(3, Types.INTEGER);                       // explicit SQL NULL
            } else {
                ps.setInt(3, p.stock());
            }
            ps.setObject(4, p.releasedOn());                        // LocalDate -> DATE (JDBC 4.2), null-safe
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return p.withId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw translate("insert " + p.name(), e);
        }
    }

    public Optional<Product> findById(long id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("select id, name, price, stock, released_on from product where id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw translate("find " + id, e);
        }
    }

    public List<Product> findCheaperThan(BigDecimal max) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "select id, name, price, stock, released_on from product where price < ? order by price")) {
            ps.setBigDecimal(1, max);
            ps.setFetchSize(100);                                   // rows per round trip for big results
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw translate("findCheaperThan", e);
        }
    }

    public int updatePrice(long id, BigDecimal price) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("update product set price = ? where id = ?")) {
            ps.setBigDecimal(1, price);
            ps.setLong(2, id);
            return ps.executeUpdate();                              // affected rows: 0 = not found
        } catch (SQLException e) {
            throw translate("updatePrice", e);
        }
    }

    public boolean delete(long id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("delete from product where id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw translate("delete", e);
        }
    }

    static Product map(ResultSet rs) throws SQLException {
        int stock = rs.getInt("stock");
        Integer nullableStock = rs.wasNull() ? null : stock;        // getInt returns 0 for NULL!
        return new Product(rs.getLong("id"), rs.getString("name"), rs.getBigDecimal("price"),
                nullableStock, rs.getObject("released_on", LocalDate.class));
    }

    /** Maps SQLState classes to meaningful unchecked exceptions (what Spring does for you). */
    static RuntimeException translate(String action, SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        if (state.startsWith("23")) {
            return new DuplicateOrInvalidDataException(action + " violates a constraint (SQLState " + state + ")", e);
        }
        return new IllegalStateException(action + " failed: " + e.getMessage(), e);
    }

    public static class DuplicateOrInvalidDataException extends RuntimeException {
        public DuplicateOrInvalidDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
