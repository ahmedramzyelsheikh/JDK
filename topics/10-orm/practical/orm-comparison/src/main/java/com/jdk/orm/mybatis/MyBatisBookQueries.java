package com.jdk.orm.mybatis;

import com.jdk.orm.BookQueries;
import com.jdk.orm.BookView;
import com.jdk.orm.mybatis.BookMapper.NewBook;
import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

/** Bootstraps MyBatis without Spring (mybatis-spring-boot-starter does this for you). */
public class MyBatisBookQueries implements BookQueries {

    private final SqlSessionFactory factory;

    public MyBatisBookQueries(DataSource dataSource) {
        Configuration config = new Configuration(new Environment("demo", new JdbcTransactionFactory(), dataSource));
        config.addMapper(BookMapper.class);
        this.factory = new SqlSessionFactoryBuilder().build(config);
    }

    @Override
    public List<BookView> booksPricierThan(BigDecimal min) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(BookMapper.class).pricierThan(min);
        }
    }

    @Override
    public long addBook(String title, BigDecimal price, long authorId) {
        try (SqlSession session = factory.openSession()) {     // manual commit mode
            NewBook book = new NewBook(title, price, authorId);
            session.getMapper(BookMapper.class).insert(book);
            session.commit();
            return book.getId();
        }
    }

    @Override
    public String style() {
        return "MyBatis (SQL mapper)";
    }
}
