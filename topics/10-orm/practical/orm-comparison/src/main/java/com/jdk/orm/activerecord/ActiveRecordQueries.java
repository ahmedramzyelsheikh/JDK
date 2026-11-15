package com.jdk.orm.activerecord;

import com.jdk.orm.BookQueries;
import com.jdk.orm.BookView;
import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;

/** Adapts the Active Record API to the common use case. */
public class ActiveRecordQueries implements BookQueries {

    public ActiveRecordQueries(DataSource dataSource) {
        BookRecord.connect(dataSource);
    }

    @Override
    public List<BookView> booksPricierThan(BigDecimal min) {
        return BookRecord.wherePriceGreaterThan(min).stream()
                .map(b -> new BookView(b.getTitle(), b.authorName(), b.getPrice()))   // 1 + N queries
                .toList();
    }

    @Override
    public long addBook(String title, BigDecimal price, long authorId) {
        return new BookRecord(title, price, authorId).save().getId();                // the object saves itself
    }

    @Override
    public String style() {
        return "Active Record";
    }
}
