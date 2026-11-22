package com.jdk.library.book;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA: an interface only. Spring generates the implementation (a proxy) at startup and
 * validates every derived query and @Query, so mistakes fail at startup, not at runtime.
 * (Repositories must be top-level interfaces: nested ones are ignored by default.)
 */
public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("""
            select new com.jdk.library.book.AuthorStats(a.name, count(b), coalesce(avg(b.price), 0))
            from Author a left join a.books b
            group by a.name
            order by count(b) desc, a.name
            """)
    List<AuthorStats> statistics();
}
