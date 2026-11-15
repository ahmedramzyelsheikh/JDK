package com.jdk.orm.jpa;

import com.jdk.orm.BookQueries;
import com.jdk.orm.BookView;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;

/**
 * JPA / Hibernate (DATA MAPPER + UNIT OF WORK). Queries are written in JPQL against ENTITIES and fields,
 * not tables and columns; Hibernate generates the SQL for the H2 dialect.
 */
public class JpaBookQueries implements BookQueries {

    private final EntityManagerFactory emf;

    public JpaBookQueries(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<BookView> booksPricierThan(BigDecimal min) {
        // constructor expression = DTO projection: one SQL query with a join, no entities loaded, no N+1
        return Jpa.inTransaction(emf, em -> em.createQuery("""
                        select new com.jdk.orm.BookView(b.title, a.name, b.price)
                        from Book b join b.author a
                        where b.price > :min
                        order by b.price desc
                        """, BookView.class)
                .setParameter("min", min)
                .getResultList());
    }

    @Override
    public long addBook(String title, BigDecimal price, long authorId) {
        return Jpa.inTransaction(emf, em -> {
            Author author = em.getReference(Author.class, authorId);   // proxy: no SELECT needed to set the FK
            Book book = new Book(title, price);
            book.assignAuthor(author);
            em.persist(book);                                          // INSERT happens at commit (flush)
            return book.getId();
        });
    }

    @Override
    public String style() {
        return "JPA/Hibernate (Data Mapper)";
    }
}
