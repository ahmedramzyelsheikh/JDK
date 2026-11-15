package com.jdk.orm.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/** Bootstraps an isolated EntityManagerFactory and offers small transaction helpers. */
public final class Jpa {

    private Jpa() {
    }

    /** One EntityManagerFactory per database (expensive: create once per application). */
    public static EntityManagerFactory create(String dbName, boolean showSql) {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        overrides.put("hibernate.show_sql", String.valueOf(showSql));
        return Persistence.createEntityManagerFactory("bookstore", overrides);
    }

    /** Same data as schema.sql, created through the object model (cascade persists the books). */
    public static void seed(EntityManagerFactory emf) {
        run(emf, em -> {
            em.persist(new Author("Joshua Bloch")
                    .addBook(new Book("Effective Java", new BigDecimal("45.00")))
                    .addBook(new Book("Java Puzzlers", new BigDecimal("30.00"))));
            em.persist(new Author("Robert Martin")
                    .addBook(new Book("Clean Code", new BigDecimal("40.00")))
                    .addBook(new Book("Clean Architecture", new BigDecimal("35.00"))));
            em.persist(new Author("Martin Fowler")
                    .addBook(new Book("Refactoring", new BigDecimal("50.00")))
                    .addBook(new Book("Patterns of Enterprise Application Architecture", new BigDecimal("55.00"))));
        });
    }

    /** Transaction without a result (a separate name avoids overload ambiguity with expression lambdas). */
    public static void run(EntityManagerFactory emf, Consumer<EntityManager> work) {
        inTransaction(emf, em -> {
            work.accept(em);
            return null;
        });
    }

    /** What Spring's @Transactional does for you: begin, run, commit - or roll back on failure. */
    public static <T> T inTransaction(EntityManagerFactory emf, Function<EntityManager, T> work) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            T result = work.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static Statistics statistics(EntityManagerFactory emf) {
        return emf.unwrap(SessionFactory.class).getStatistics();
    }
}
