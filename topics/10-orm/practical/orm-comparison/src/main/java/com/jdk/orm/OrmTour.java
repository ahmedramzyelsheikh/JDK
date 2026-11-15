package com.jdk.orm;

import com.jdk.orm.activerecord.ActiveRecordQueries;
import com.jdk.orm.jdbc.JdbcBookGateway;
import com.jdk.orm.jdbi.JdbiBookQueries;
import com.jdk.orm.jpa.Book;
import com.jdk.orm.jpa.Jpa;
import com.jdk.orm.jpa.JpaBookQueries;
import com.jdk.orm.mybatis.MyBatisBookQueries;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.stat.Statistics;

/**
 * Runs the same use case with every style, then shows Hibernate's N+1 problem and its fix
 * with the SQL printed by Hibernate.
 * Run: ./mvnw -q -f topics/pom.xml -pl 10-orm/practical/orm-comparison compile exec:java
 */
public class OrmTour {

    public static void main(String[] args) {
        List<BookQueries> styles = List.of(
                new JdbcBookGateway(Db.freshDatabase("tour-jdbc")),
                new ActiveRecordQueries(Db.freshDatabase("tour-ar")),
                new MyBatisBookQueries(Db.freshDatabase("tour-mybatis")),
                new JdbiBookQueries(Db.freshDatabase("tour-jdbi")),
                jpaStyle());

        for (BookQueries q : styles) {
            long id = q.addBook("Domain-Driven Design", new BigDecimal("60.00"), 3);
            System.out.printf("%n== %s ==  (inserted book id %d)%n", q.style(), id);
            q.booksPricierThan(new BigDecimal("44")).forEach(b ->
                    System.out.printf("  %-50s %-14s %6s%n", b.title(), b.author(), b.price()));
        }

        System.out.println("\n== Hibernate: N+1 vs JOIN FETCH (SQL printed by Hibernate) ==");
        EntityManagerFactory emf = Jpa.create("tour-nplus1", true);
        try {
            Jpa.seed(emf);
            Statistics stats = Jpa.statistics(emf);

            stats.clear();
            System.out.println("-- lazy author loaded per book:");
            Jpa.run(emf, em -> {
                for (Book b : em.createQuery("select b from Book b order by b.id", Book.class).getResultList()) {
                    b.getAuthor().getName();
                }
            });
            System.out.println("   => " + stats.getPrepareStatementCount() + " SQL statements (1 for books + 1 per distinct author)");

            stats.clear();
            System.out.println("-- join fetch:");
            Jpa.run(emf, em -> {
                for (Book b : em.createQuery("select b from Book b join fetch b.author order by b.id", Book.class).getResultList()) {
                    b.getAuthor().getName();
                }
            });
            System.out.println("   => " + stats.getPrepareStatementCount() + " SQL statement");
        } finally {
            emf.close();
        }
    }

    private static BookQueries jpaStyle() {
        EntityManagerFactory emf = Jpa.create("tour-jpa", false);
        Jpa.seed(emf);
        return new JpaBookQueries(emf);
    }
}
