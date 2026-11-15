package com.jdk.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import liquibase.command.CommandScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LiquibaseMigrationTest {

    static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";

    String url;
    Connection keepAlive;

    @BeforeEach
    void freshDatabase() throws SQLException {
        url = "jdbc:h2:mem:lb" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1";
        keepAlive = DriverManager.getConnection(url, "sa", "");
    }

    @AfterEach
    void close() throws SQLException {
        keepAlive.close();
    }

    private void liquibase(String command, Object... extraArgs) throws Exception {
        CommandScope scope = new CommandScope(command)
                .addArgumentValue("changelogFile", CHANGELOG)
                .addArgumentValue("url", url)
                .addArgumentValue("username", "sa")
                .addArgumentValue("password", "");
        for (int i = 0; i < extraArgs.length; i += 2) {
            scope.addArgumentValue((String) extraArgs[i], extraArgs[i + 1]);
        }
        scope.execute();
    }

    private List<String> rows(String sql) throws SQLException {
        try (Statement st = keepAlive.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<String> out = new ArrayList<>();
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    sb.append(i > 1 ? " | " : "").append(rs.getString(i));
                }
                out.add(sb.toString());
            }
            return out;
        }
    }

    @Test
    void updateAppliesAllChangesetsAndRecordsThem() throws Exception {
        liquibase("update");

        assertThat(rows("select id, author from databasechangelog order by orderexecuted")).containsExactly(
                "1-create-product | jdk-course", "2-seed-products | jdk-course", "3-add-product-stock | jdk-course");
        assertThat(rows("select name, price, stock from product order by id"))
                .containsExactly("Laptop | 1200.00 | 0", "Mouse | 25.00 | 0");
    }

    @Test
    void rollbackCountUndoesTheLastChangesets() throws Exception {
        liquibase("update");

        liquibase("rollbackCount", "count", 2);                             // undo 3 (drop column) and 2 (delete rows)

        assertThat(rows("select id from databasechangelog")).containsExactly("1-create-product");
        assertThat(rows("select count(*) from product")).containsExactly("0");
        assertThat(rows("select count(*) from information_schema.columns where table_name = 'PRODUCT' and column_name = 'STOCK'"))
                .containsExactly("0");
    }
}
