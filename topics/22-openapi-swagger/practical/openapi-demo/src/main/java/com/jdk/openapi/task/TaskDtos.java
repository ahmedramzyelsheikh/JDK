package com.jdk.openapi.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTOs documented with @Schema. Bean Validation constraints (@NotBlank, @Size...) are ALSO read by springdoc
 * and appear in the spec as required / minLength / maxLength - one source of truth.
 */
public final class TaskDtos {

    private TaskDtos() {
    }

    @Schema(description = "Task priority")
    public enum Priority { LOW, MEDIUM, HIGH }

    @Schema(description = "Data needed to create a task")
    public record CreateTaskRequest(
            @Schema(description = "Short title", example = "Write OpenAPI docs")
            @NotBlank @Size(max = 80) String title,
            @Schema(description = "Priority, MEDIUM if omitted", example = "HIGH")
            Priority priority,
            @Schema(description = "Due date (ISO-8601)", example = "2026-12-01")
            @NotNull @FutureOrPresent LocalDate dueOn) { }

    @Schema(description = "A task")
    public record TaskResponse(
            @Schema(description = "Server-generated id", example = "1", accessMode = Schema.AccessMode.READ_ONLY) long id,
            @Schema(example = "Write OpenAPI docs") String title,
            Priority priority,
            @Schema(example = "2026-12-01") LocalDate dueOn,
            @Schema(description = "true when completed") boolean done) { }
}
