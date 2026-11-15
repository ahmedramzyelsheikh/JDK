package com.jdk.orm.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.hibernate.LazyInitializationException;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Executable documentation of the Hibernate concepts in the README. */
class HibernateBehaviourTest {

    private EntityManagerFactory emf;
    private Statistics stats;

    @BeforeEach
    void setUp() {
        emf = Jpa.create("hb" + UUID.randomUUID().toString().replace("-", ""), false);
        Jpa.seed(emf);
        stats = Jpa.statistics(emf);
        stats.clear();
    }

    @AfterEach
    void tearDown() {
        emf.close();
    }

    private Long idOf(String title) {
        return Jpa.inTransaction(emf, em -> em.createQuery("select b.id from Book b where b.title = :t", Long.class)
                .setParameter("t", title).getSingleResult());
    }

    @Test
    void entityLifecycleStates() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Author author = new Author("Kent Beck");                    // TRANSIENT
        assertThat(em.contains(author)).isFalse();

        em.persist(author);                                         // MANAGED
        assertThat(em.contains(author)).isTrue();
        assertThat(author.getId()).isNotNull();                     // sequence value assigned, but INSERT still pending
        em.flush();                                                 // write the INSERT now; without it, detach() would
                                                                    // drop the pending insert and merge() below would fail

        em.detach(author);                                          // DETACHED
        assertThat(em.contains(author)).isFalse();

        author.setName("Kent Beck (TDD)");
        Author managedCopy = em.merge(author);                      // merge returns ANOTHER, managed instance
        assertThat(managedCopy).isNotSameAs(author);
        assertThat(em.contains(managedCopy)).isTrue();
        assertThat(em.contains(author)).isFalse();

        em.remove(managedCopy);                                     // REMOVED
        assertThat(em.contains(managedCopy)).isFalse();
        em.getTransaction().commit();
        em.close();

        Author afterCommit = Jpa.inTransaction(emf, e -> e.find(Author.class, managedCopy.getId()));
        assertThat(afterCommit).isNull();
    }

    @Test
    void dirtyCheckingUpdatesWithoutCallingUpdate() {
        Long id = idOf("Clean Code");
        stats.clear();

        Jpa.run(emf, em -> em.find(Book.class, id).setPrice(new BigDecimal("42.00")));   // no save()/update()!

        assertThat(stats.getEntityUpdateCount()).isEqualTo(1);
        BigDecimal stored = Jpa.inTransaction(emf, em -> em.find(Book.class, id).getPrice());
        assertThat(stored).isEqualByComparingTo("42.00");
    }

    @Test
    void identityMapReturnsSameInstanceWithOneSelect() {
        Long id = idOf("Refactoring");
        stats.clear();

        Jpa.run(emf, em -> {
            Book first = em.find(Book.class, id);
            Book second = em.find(Book.class, id);                  // served from the persistence context
            assertThat(second).isSameAs(first);
        });

        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void lazyAssociationCausesNPlusOneAndJoinFetchFixesIt() {
        Jpa.run(emf, em -> em.createQuery("select b from Book b", Book.class).getResultList()
                .forEach(b -> b.getAuthor().getName()));
        long lazy = stats.getPrepareStatementCount();

        stats.clear();
        Jpa.run(emf, em -> em.createQuery("select b from Book b join fetch b.author", Book.class).getResultList()
                .forEach(b -> b.getAuthor().getName()));
        long fetched = stats.getPrepareStatementCount();

        assertThat(lazy).isEqualTo(1 + 3);       // 1 query for books + 1 per distinct author (identity map caches repeats)
        assertThat(fetched).isEqualTo(1);
    }

    @Test
    void accessingLazyDataAfterTheContextClosedFails() {
        Long id = idOf("Effective Java");
        Book detached = Jpa.inTransaction(emf, em -> em.find(Book.class, id));     // author is an uninitialised proxy

        assertThatThrownBy(() -> detached.getAuthor().getName())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void optimisticLockingDetectsConcurrentUpdates() {
        Long id = idOf("Java Puzzlers");
        EntityManager alice = emf.createEntityManager();
        EntityManager bob = emf.createEntityManager();
        alice.getTransaction().begin();
        bob.getTransaction().begin();
        Book aliceCopy = alice.find(Book.class, id);                 // both read version 0
        Book bobCopy = bob.find(Book.class, id);

        aliceCopy.setPrice(new BigDecimal("31.00"));
        alice.getTransaction().commit();                             // version becomes 1

        bobCopy.setPrice(new BigDecimal("29.00"));
        assertThatThrownBy(() -> bob.getTransaction().commit())      // UPDATE ... WHERE version = 0 -> 0 rows
                .isInstanceOf(RollbackException.class)
                .hasCauseInstanceOf(OptimisticLockException.class);
        alice.close();
        bob.close();

        BigDecimal winner = Jpa.inTransaction(emf, em -> em.find(Book.class, id).getPrice());
        assertThat(winner).isEqualByComparingTo("31.00");
    }

    @Test
    void orphanRemovalDeletesBooksRemovedFromTheCollection() {
        Jpa.run(emf, em -> {
            Author fowler = em.createQuery("select a from Author a where a.name = 'Martin Fowler'", Author.class).getSingleResult();
            Book refactoring = fowler.getBooks().stream().filter(b -> b.getTitle().equals("Refactoring")).findFirst().orElseThrow();
            fowler.removeBook(refactoring);
        });

        Long remaining = Jpa.inTransaction(emf, em -> em.createQuery("select count(b) from Book b", Long.class).getSingleResult());
        assertThat(remaining).isEqualTo(5);
    }

    @Test
    void singleTableInheritanceStoresSubclassesInOneTable() {
        Jpa.run(emf, em -> {
            em.persist(new CardPayment(new BigDecimal("99.90"), "4242"));
            em.persist(new BankTransfer(new BigDecimal("500.00"), "EG380019000500000000263180002"));
        });

        List<String> polymorphic = Jpa.inTransaction(emf, em -> em.createQuery("select p from Payment p order by p.amount", Payment.class)
                .getResultList().stream().map(Payment::describe).toList());
        List<String> types = Jpa.inTransaction(emf, em -> ((List<?>) em.createNativeQuery("select type from payment order by type")
                .getResultList()).stream().map(String::valueOf).toList());

        assertThat(polymorphic).containsExactly("card ****4242 99.90", "transfer from EG380019000500000000263180002 500.00");
        assertThat(types).containsExactly("BANK", "CARD");          // discriminator column in the single table
    }
}
