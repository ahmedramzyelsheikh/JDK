package com.jdk.orm.jdbc;

import com.jdk.orm.BookQueries;
import com.jdk.orm.BookView;
import com.jdk.orm.Db.DataAccessException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * Raw JDBC, TABLE DATA GATEWAY pattern: one object handles all SQL for the book table.
 * Everything is explicit: connections, statements, parameters, ResultSet mapping, keys, exceptions.
 */
public class JdbcBookGateway implements BookQueries {

    private static final String PRICIER_THAN = """
            select b.title, a.name, b.price
            from book b join author a on a.id = b.author_id
            where b.price > ?
            order by b.price desc
            """;

    private final DataSource dataSource;

    public JdbcBookGateway(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<BookView> booksPricierThan(BigDecimal min) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(PRICIER_THAN)) {     // PreparedStatement: no SQL injection
            ps.setBigDecimal(1, min);
            try (ResultSet rs = ps.executeQuery()) {
                List<BookView> result = new ArrayList<>();
                while (rs.next()) {                                           // manual row -> object mapping
                    result.add(new BookView(rs.getString("title"), rs.getString("name"), rs.getBigDecimal("price")));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("query failed", e);
        }
    }

    @Override
    public long addBook(String title, BigDecimal price, long authorId) {
        String sql = "insert into book (title, price, author_id) values (?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setBigDecimal(2, price);
            ps.setLong(3, authorId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("insert failed", e);
        }
    }

    @Override
    public String style() {
        return "JDBC (Table Data Gateway)";
    }
}
