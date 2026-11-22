package com.jdk.library.book;

import com.jdk.library.book.BookDtos.BookResponse;
import com.jdk.library.book.BookDtos.ChangePriceRequest;
import com.jdk.library.book.BookDtos.CreateBookRequest;
import com.jdk.library.book.BookDtos.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping("/books")
    public PageResponse<BookResponse> search(@RequestParam(defaultValue = "") String title,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(defaultValue = "title") String sort) {
        return service.search(title, page, size, sort);
    }

    @GetMapping("/books/{id}")
    public BookResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping("/books")
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        BookResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/books/" + created.id())).body(created);
    }

    @PatchMapping("/books/{id}/price")
    public BookResponse changePrice(@PathVariable long id, @Valid @RequestBody ChangePriceRequest request) {
        return service.changePrice(id, request.price());
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/authors/stats")
    public List<AuthorStats> stats() {
        return service.authorStatistics();
    }
}
