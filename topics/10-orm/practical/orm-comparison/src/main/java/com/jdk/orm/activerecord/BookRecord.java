package com.jdk.orm.activerecord;

import com.jdk.orm.Db.DataAccessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * ACTIVE RECORD pattern: the object wraps a row, carries business logic AND knows how to load/save itself
 * (this is how Rails, Django, Laravel Eloquent and Quarkus Panache entities feel).
 * Productive for CRUD, but the domain is now coupled to the database (hard to unit-test without a DB).
 */
public class BookRecord {

    private static DataSource dataSource;            // "connection" shared by all records

    private Long id;
    private String title;
    private BigDecimal price;
    private long authorId;

    public static void connect(DataSource ds) {
        dataSource = ds;
    }

    public BookRecord(String title, BigDecimal price, long authorId) {
        this.title = title;
        this.price = price;
        this.authorId = authorId;
    }

    private BookRecord(long id, String title, BigDecimal price, long authorId) {
        this(title, price, authorId);
        this.id = id;
    }

    // ---------------- business logic lives here too ----------------
    public BookRecord applyDiscount(int percent) {
        price = price.multiply(BigDecimal.valueOf(100 - percent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return this;
    }

    // ---------------- persistence methods on the object itself ----------------
    public BookRecord save() {
        try (Connection c = dataSource.getConnection()) {
            if (id == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "insert into book (title, price, author_id) values (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, title);
                    ps.setBigDecimal(2, price);
                    ps.setLong(3, authorId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getLong(1);
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("update book set title = ?, price = ?, author_id = ? where id = ?")) {
                    ps.setString(1, title);
                    ps.setBigDecimal(2, price);
                    ps.setLong(3, authorId);
                    ps.setLong(4, id);
                    ps.executeUpdate();
                }
            }
            return this;
        } catch (SQLException e) {
            throw new DataAccessException("save failed", e);
        }
    }

    public void delete() {
        execute("delete from book where id = " + id);
        id = null;
    }

    // ---------------- static finders ----------------
    public static Optional<BookRecord> find(long id) {
        List<BookRecord> found = query("select id, title, price, author_id from book where id = ?", id);
        return found.stream().findFirst();
    }

    public static List<BookRecord> wherePriceGreaterThan(BigDecimal min) {
        return query("select id, title, price, author_id from book where price > ? order by price desc", min);
    }

    /** Navigating to the author from an Active Record usually means one more query per record (N+1!). */
    public String authorName() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("select name from author where id = ?")) {
            ps.setLong(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("author lookup failed", e);
        }
    }

    private static List<BookRecord> query(String sql, Object param) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                List<BookRecord> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new BookRecord(rs.getLong("id"), rs.getString("title"), rs.getBigDecimal("price"), rs.getLong("author_id")));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("query failed", e);
        }
    }

    private static void execute(String sql) {
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new DataAccessException("statement failed", e);
        }
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public BigDecimal getPrice() { return price; }
}
