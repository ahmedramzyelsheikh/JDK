package db.migration;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Java-based migration: for logic that is awkward in SQL (computed data, calling APIs, complex transforms).
 * Discovered by name and package (db.migration) like SQL migrations.
 */
public class V5__Seed_countries extends BaseJavaMigration {

    private static final Map<String, String> COUNTRIES = Map.of("EG", "Egypt", "SA", "Saudi Arabia", "AE", "United Arab Emirates");

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement st = context.getConnection().createStatement()) {
            st.execute("create table country (code char(2) primary key, name varchar(60) not null)");
        }
        try (PreparedStatement ps = context.getConnection().prepareStatement("insert into country (code, name) values (?, ?)")) {
            for (var e : COUNTRIES.entrySet()) {
                ps.setString(1, e.getKey());
                ps.setString(2, e.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
