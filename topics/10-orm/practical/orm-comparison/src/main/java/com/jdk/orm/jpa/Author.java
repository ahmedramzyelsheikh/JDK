package com.jdk.orm.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DATA MAPPER style entity: a plain domain object with mapping metadata (annotations).
 * It never opens connections or writes SQL - the EntityManager does that.
 */
@Entity
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Inverse side ("mappedBy"): the FK lives in book.author_id, owned by Book.author. */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books = new ArrayList<>();

    protected Author() {                   // required by JPA
    }

    public Author(String name) {
        this.name = name;
    }

    /** Keeps both sides of the bidirectional relationship in sync. */
    public Author addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
        return this;
    }

    public void removeBook(Book book) {
        books.remove(book);
        book.setAuthor(null);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Book> getBooks() { return Collections.unmodifiableList(books); }
}
