package com.jdk.library.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;

/** API contract (DTOs). Entities never leave the service layer. */
public final class BookDtos {

    private BookDtos() {
    }

    public record CreateBookRequest(
            @NotBlank String title,
            @NotBlank @Pattern(regexp = "\\d{13}", message = "must be 13 digits") String isbn,
            @NotNull Long authorId,
            @NotNull @Positive BigDecimal price,
            @PastOrPresent LocalDate publishedOn) { }

    public record ChangePriceRequest(@NotNull @Positive BigDecimal price) { }

    public record BookResponse(Long id, String title, String isbn, BigDecimal price, LocalDate publishedOn, String author) {
        static BookResponse from(Book b) {
            return new BookResponse(b.getId(), b.getTitle(), b.getIsbn(), b.getPrice(), b.getPublishedOn(), b.getAuthor().getName());
        }
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        static <T> PageResponse<T> from(Page<T> page) {
            return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }
}
