package com.jdk.orm.jdbi;

import com.jdk.orm.BookQueries;
import com.jdk.orm.BookView;
import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;

/**
 * JDBI, a MICRO-ORM: SQL stays explicit, but connections, statements, binding and mapping
 * shrink to a fluent one-liner. (Dapper is the .NET equivalent.)
 */
public class JdbiBookQueries implements BookQueries {

    private final Jdbi jdbi;

    public JdbiBookQueries(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource);
    }

    @Override
    public List<BookView> booksPricierThan(BigDecimal min) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select b.title, a.name, b.price
                        from book b join author a on a.id = b.author_id
                        where b.price > :min
                        order by b.price desc
                        """)
                .bind("min", min)
                .map((rs, ctx) -> new BookView(rs.getString("title"), rs.getString("name"), rs.getBigDecimal("price")))
                .list());
    }

    @Override
    public long addBook(String title, BigDecimal price, long authorId) {
        return jdbi.withHandle(handle -> handle
                .createUpdate("insert into book (title, price, author_id) values (:title, :price, :authorId)")
                .bind("title", title)
                .bind("price", price)
                .bind("authorId", authorId)
                .executeAndReturnGeneratedKeys()
                .mapTo(Long.class)
                .one());
    }

    @Override
    public String style() {
        return "JDBI (micro-ORM)";
    }
}
