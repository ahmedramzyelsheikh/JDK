package com.jdk.mvc.task;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.jdk.mvc.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test: starts only the MVC layer (controllers, advice, Thymeleaf) - no full application.
 * The real service + in-memory repository are imported so we test real behaviour end to end.
 */
@WebMvcTest(TaskController.class)
@Import({TaskService.class, TaskRepository.class, GlobalExceptionHandler.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void cleanRepository() {
        // the Spring context (and this singleton bean) is reused by every test: start each test empty
        repository.findAll().forEach(t -> repository.deleteById(t.id()));
    }

    @Test
    void listRendersTheTasksViewWithAnEmptyForm() throws Exception {
        mvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks"))
                .andExpect(model().attributeExists("tasks", "taskForm"))
                .andExpect(content().string(containsString("<h1>Tasks</h1>")));
    }

    @Test
    void validPostRedirectsWithFlashMessage() throws Exception {
        mvc.perform(post("/tasks").param("title", "Learn Spring MVC").param("priority", "HIGH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("message", "Task 'Learn Spring MVC' created"));

        mvc.perform(get("/tasks"))
                .andExpect(model().attribute("tasks", hasSize(1)))
                .andExpect(content().string(containsString("Learn Spring MVC")));
    }

    @Test
    void invalidPostReRendersFormWithErrors() throws Exception {
        mvc.perform(post("/tasks").param("title", " ").param("priority", "LOW"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks"))
                .andExpect(model().attributeHasFieldErrors("taskForm", "title"))
                .andExpect(content().string(containsString("Title is required")));
    }

    @Test
    void duplicateTitleIsRejectedByTheBusinessRule() throws Exception {
        mvc.perform(post("/tasks").param("title", "Write tests").param("priority", "LOW"));

        mvc.perform(post("/tasks").param("title", "WRITE TESTS").param("priority", "LOW"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("taskForm", "title", "duplicate"));
    }

    @Test
    void togglingAnUnknownTaskShowsThe404Page() throws Exception {
        mvc.perform(post("/tasks/999/toggle"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"))
                .andExpect(content().string(containsString("Task #999 does not exist")));
    }

    @Test
    void userInputIsHtmlEscaped() throws Exception {
        mvc.perform(post("/tasks").param("title", "<script>alert(1)</script>").param("priority", "LOW"));

        mvc.perform(get("/tasks"))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }
}
