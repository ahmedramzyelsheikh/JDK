package com.jdk.library.book;

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
import java.time.LocalDate;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(unique = true)
    private String isbn;

    private BigDecimal price;

    private LocalDate publishedOn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private Author author;

    @Version
    private int version;

    protected Book() {
    }

    public Book(String title, String isbn, BigDecimal price, LocalDate publishedOn, Author author) {
        this.title = title;
        this.isbn = isbn;
        this.price = price;
        this.publishedOn = publishedOn;
        this.author = author;
    }

    public void changePrice(BigDecimal newPrice) {
        if (newPrice.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        this.price = newPrice;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getIsbn() { return isbn; }
    public BigDecimal getPrice() { return price; }
    public LocalDate getPublishedOn() { return publishedOn; }
    public Author getAuthor() { return author; }
    public int getVersion() { return version; }
}
