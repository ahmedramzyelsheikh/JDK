package com.jdk.sql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

/** Creates a fresh in-memory H2 database (PostgreSQL mode) with the shop schema and data. */
public final class Database {

    private final String url;
    /**
     * Connection that created the schema, kept open for the life of the database. With H2 2.4.240, CHECK
     * constraints fail with "Check constraint invalid ... database has been closed" in other sessions once the
     * session that created them is closed. Keeping it open avoids that quirk (a real PostgreSQL has no such issue).
     */
    private Connection schemaOwner;

    private Database(String url) {
        this.url = url;
    }

    public static Database createShop() {
        Database db = new Database("jdbc:h2:mem:shop" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        try {
            db.schemaOwner = db.connect();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        db.runScript("/sql/schema.sql");
        db.runScript("/sql/data.sql");
        return db;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }

    /** Runs a query and returns each row as "col1 | col2 | ..." - easy to assert on. */
    public List<String> rows(String sql) {
        try (Connection c = connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return toRows(rs);
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public int update(String sql) {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static List<String> toRows(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        List<String> rows = new ArrayList<>();
        while (rs.next()) {
            StringJoiner row = new StringJoiner(" | ");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object value = rs.getObject(i);
                row.add(value instanceof java.math.BigDecimal bd ? bd.stripTrailingZeros().toPlainString() : String.valueOf(value));
            }
            rows.add(row.toString());
        }
        return rows;
    }

    private void runScript(String resource) {
        try (InputStream in = Database.class.getResourceAsStream(resource); Statement st = schemaOwner.createStatement()) {
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder current = new StringBuilder();
            for (String line : script.lines().toList()) {
                String code = line.contains("--") ? line.substring(0, line.indexOf("--")) : line;
                current.append(code).append('\n');
                if (code.strip().endsWith(";")) {
                    st.execute(current.toString().strip().replaceAll(";$", ""));
                    current.setLength(0);
                }
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("failed to run " + resource + ": " + e.getMessage(), e);
        }
    }
}
