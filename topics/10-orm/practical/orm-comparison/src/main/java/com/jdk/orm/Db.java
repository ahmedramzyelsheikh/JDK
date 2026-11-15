package com.jdk.orm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

/** Helpers to create isolated in-memory H2 databases. */
public final class Db {

    private Db() {
    }

    /** A brand-new in-memory database with schema.sql applied. */
    public static DataSource freshDatabase(String name) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        runScript(ds, "/schema.sql");
        return ds;
    }

    static void runScript(DataSource ds, String resource) {
        try (InputStream in = Db.class.getResourceAsStream(resource);
             Connection c = ds.getConnection();
             Statement st = c.createStatement()) {
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : script.split(";")) {
                String withoutComments = sql.lines().filter(l -> !l.strip().startsWith("--")).reduce("", (a, b) -> a + "\n" + b);
                if (!withoutComments.isBlank()) {
                    st.execute(withoutComments);
                }
            }
        } catch (IOException | SQLException e) {
            throw new DataAccessException("could not run " + resource, e);
        }
    }

    /** Unchecked wrapper, like Spring's DataAccessException. */
    public static class DataAccessException extends RuntimeException {
        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
