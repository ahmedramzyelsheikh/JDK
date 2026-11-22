package com.jdk.library.book;

import com.jdk.library.book.BookDtos.BookResponse;
import com.jdk.library.book.BookDtos.CreateBookRequest;
import com.jdk.library.book.BookDtos.PageResponse;
import com.jdk.library.config.LibraryProperties;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use cases. Transactions are declared here (not in controllers or repositories). */
@Service
@Transactional(readOnly = true)                            // default for all methods: read-only transactions
public class BookService {

    private final BookRepository books;
    private final AuthorRepository authors;
    private final LibraryProperties properties;

    public BookService(BookRepository books, AuthorRepository authors, LibraryProperties properties) {
        this.books = books;
        this.authors = authors;
        this.properties = properties;
    }

    public PageResponse<BookResponse> search(String title, int page, int size, String sortBy) {
        int safeSize = Math.min(size, properties.maxPageSize());             // configuration in action
        PageRequest pageable = PageRequest.of(page, safeSize, Sort.by(sortBy));
        return PageResponse.from(books.findByTitleContainingIgnoreCase(title, pageable).map(BookResponse::from));
    }

    public BookResponse get(long id) {
        return books.findWithAuthorById(id).map(BookResponse::from).orElseThrow(() -> new NotFoundException("book", id));
    }

    @Transactional                                          // read-write
    public BookResponse create(CreateBookRequest request) {
        if (books.existsByIsbn(request.isbn())) {
            throw new ConflictException("a book with ISBN " + request.isbn() + " already exists");
        }
        Author author = authors.findById(request.authorId()).orElseThrow(() -> new NotFoundException("author", request.authorId()));
        Book saved = books.save(new Book(request.title(), request.isbn(), request.price(), request.publishedOn(), author));
        return BookResponse.from(saved);
    }

    @Transactional
    public BookResponse changePrice(long id, BigDecimal price) {
        Book book = books.findWithAuthorById(id).orElseThrow(() -> new NotFoundException("book", id));
        book.changePrice(price);                            // no save(): dirty checking writes it at commit (topic 10)
        return BookResponse.from(book);
    }

    @Transactional
    public void delete(long id) {
        if (!books.existsById(id)) {
            throw new NotFoundException("book", id);
        }
        books.deleteById(id);
    }

    public List<AuthorStats> authorStatistics() {
        return authors.statistics();
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String what, long id) {
            super(what + " " + id + " does not exist");
        }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }
}
