package com.jdk.rest.product;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jdk.rest.error.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ProductController.class)
@Import({ProductService.class, ProductRepository.class, ApiExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void reset() {
        repository.deleteAll();
    }

    private ResultActions create(String json) throws Exception {
        return mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void postCreatesAndReturns201WithLocationAndEtag() throws Exception {
        create("{\"name\":\"Laptop\",\"category\":\"tech\",\"price\":999.5,\"stock\":5}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/1"))
                .andExpect(header().string("ETag", "\"1\""))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.version").doesNotExist());     // internal field not exposed
    }

    @Test
    void invalidBodyReturns400ProblemDetailWithFieldErrors() throws Exception {
        create("{\"name\":\"\",\"category\":\"tech\",\"price\":-1}")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").value("must be greater than 0"));
    }

    @Test
    void duplicateNameReturns409() throws Exception {
        create("{\"name\":\"Mug\",\"category\":\"home\",\"price\":5}");

        create("{\"name\":\"MUG\",\"category\":\"home\",\"price\":6}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("already exists")));
    }

    @Test
    void unknownIdReturns404ProblemDetail() throws Exception {
        mvc.perform(get("/api/products/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.type").value("https://api.jdk-course.example/problems/not-found"))
                .andExpect(jsonPath("$.instance").value("/api/products/42"));
    }

    @Test
    void listFiltersSortsAndPaginates() throws Exception {
        create("{\"name\":\"A\",\"category\":\"tech\",\"price\":30}");
        create("{\"name\":\"B\",\"category\":\"tech\",\"price\":10}");
        create("{\"name\":\"C\",\"category\":\"home\",\"price\":20}");
        create("{\"name\":\"D\",\"category\":\"tech\",\"price\":50}");

        mvc.perform(get("/api/products").param("category", "tech").param("sort", "price,desc").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("D"))
                .andExpect(jsonPath("$.content[1].name").value("A"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void invalidSortFieldReturns400() throws Exception {
        mvc.perform(get("/api/products").param("sort", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("cannot sort by 'password'")));
    }

    @Test
    void putWithStaleEtagReturns412() throws Exception {
        create("{\"name\":\"Phone\",\"category\":\"tech\",\"price\":300}");
        String body = "{\"name\":\"Phone 2\",\"category\":\"tech\",\"price\":350,\"stock\":1}";

        mvc.perform(put("/api/products/1").header("If-Match", "\"1\"").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"2\""));

        mvc.perform(put("/api/products/1").header("If-Match", "\"1\"").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void patchChangesOnlyGivenFields() throws Exception {
        create("{\"name\":\"Desk\",\"category\":\"home\",\"price\":120,\"stock\":2}");

        mvc.perform(patch("/api/products/1").contentType(MediaType.APPLICATION_JSON).content("{\"stock\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(9))
                .andExpect(jsonPath("$.name").value("Desk"))
                .andExpect(jsonPath("$.price").value(120));
    }

    @Test
    void deleteReturns204ThenResourceIsGone() throws Exception {
        create("{\"name\":\"Lamp\",\"category\":\"home\",\"price\":15}");

        mvc.perform(delete("/api/products/1")).andExpect(status().isNoContent());
        mvc.perform(get("/api/products/1")).andExpect(status().isNotFound());
    }

    @Test
    void wrongContentTypeReturns415AsProblemDetail() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
