package com.jdk.library.book;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jdk.library.book.BookDtos.BookResponse;
import com.jdk.library.book.BookService.NotFoundException;
import com.jdk.library.web.ApiExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web slice: controller + JSON + validation + error handling; the service is a Mockito mock bean. */
@WebMvcTest(BookController.class)
@Import(ApiExceptionHandler.class)
class BookControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean                                       // replaces the BookService bean in the context with a mock
    BookService service;

    @Test
    void getReturnsTheBookAsJson() throws Exception {
        given(service.get(1L)).willReturn(new BookResponse(1L, "Effective Java", "9780134685991",
                new BigDecimal("45.00"), LocalDate.of(2018, 1, 6), "Joshua Bloch"));

        mvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.publishedOn").value("2018-01-06"))      // java.time as ISO string
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));
    }

    @Test
    void unknownBookIs404ProblemDetail() throws Exception {
        given(service.get(99L)).willThrow(new NotFoundException("book", 99));

        mvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("book 99 does not exist"));
    }

    @Test
    void invalidRequestNeverReachesTheService() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"isbn\":\"123\",\"authorId\":1,\"price\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.isbn").value("must be 13 digits"))
                .andExpect(jsonPath("$.errors.price").exists());

        verifyNoInteractions(service);
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        given(service.create(any())).willReturn(new BookResponse(6L, "PoEAA", "9780321127426",
                new BigDecimal("55"), null, "Martin Fowler"));

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"PoEAA\",\"isbn\":\"9780321127426\",\"authorId\":3,\"price\":55}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/6"));
    }
}
