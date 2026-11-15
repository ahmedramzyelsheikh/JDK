package com.jdk.orm.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Owning side of the relationship: the author_id FK column. LAZY (default for ManyToOne is EAGER!). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    /** Optimistic locking: UPDATE ... WHERE id = ? AND version = ? */
    @Version
    private int version;

    protected Book() {
    }

    public Book(String title, BigDecimal price) {
        this.title = title;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Author getAuthor() { return author; }
    void setAuthor(Author author) { this.author = author; }
    public void assignAuthor(Author author) { this.author = author; }
    public int getVersion() { return version; }

    /** Id-based equality with a constant hashCode: safe across transient/managed/detached states. */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Book other && id != null && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return Book.class.hashCode();
    }
}
