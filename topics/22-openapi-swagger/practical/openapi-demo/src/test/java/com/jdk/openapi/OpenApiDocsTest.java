package com.jdk.openapi;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The generated spec is part of the contract: these tests fail if an endpoint, schema or rule disappears.
 * They also export the spec to target/openapi.json (e.g. for client generation or contract checks in CI).
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired
    MockMvc mvc;

    @Test
    void specDescribesInfoPathsAndOperations() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value(containsString("3.")))
                .andExpect(jsonPath("$.info.title").value("Task API"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.paths['/api/tasks'].get.summary").value("List tasks"))
                .andExpect(jsonPath("$.paths['/api/tasks'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/tasks/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/tasks/{id}'].delete.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.tags[*].name").value(hasItem("Tasks")));
    }

    @Test
    void validationRulesBecomeSchemaConstraints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.schemas.CreateTaskRequest.required").value(hasItems("title", "dueOn")))
                .andExpect(jsonPath("$.components.schemas.CreateTaskRequest.properties.title.maxLength").value(80))
                .andExpect(jsonPath("$.components.schemas.CreateTaskRequest.properties.title.example").value("Write OpenAPI docs"))
                .andExpect(jsonPath("$.components.schemas.CreateTaskRequest.properties.priority.enum").value(hasItems("LOW", "MEDIUM", "HIGH")))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void specIsAlsoAvailableAsYamlAndCanBeExported() throws Exception {
        mvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("title: Task API")));

        String json = mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/openapi.json"), json);
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }
}
