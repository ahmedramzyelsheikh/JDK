package com.jdk.orm.mybatis;

import com.jdk.orm.BookView;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis SQL MAPPER: you write the SQL (here in annotations, often in XML files);
 * MyBatis binds parameters (#{...} = PreparedStatement placeholders) and maps rows to objects.
 * The interface has no implementation: MyBatis creates a dynamic proxy for it.
 */
public interface BookMapper {

    @Select("""
            select b.title, a.name as author, b.price
            from book b join author a on a.id = b.author_id
            where b.price > #{min}
            order by b.price desc
            """)
    List<BookView> pricierThan(@Param("min") BigDecimal min);       // mapped into the record's constructor

    @Insert("insert into book (title, price, author_id) values (#{title}, #{price}, #{authorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NewBook book);

    /** Mutable parameter object so MyBatis can write the generated key back into it. */
    class NewBook {
        private Long id;
        private final String title;
        private final BigDecimal price;
        private final long authorId;

        public NewBook(String title, BigDecimal price, long authorId) {
            this.title = title;
            this.price = price;
            this.authorId = authorId;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public BigDecimal getPrice() { return price; }
        public long getAuthorId() { return authorId; }
    }
}
