package com.jdk.openapi.task;

import com.jdk.openapi.task.TaskDtos.CreateTaskRequest;
import com.jdk.openapi.task.TaskDtos.Priority;
import com.jdk.openapi.task.TaskDtos.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Create, list and delete tasks")
public class TaskController {

    private final Map<Long, TaskResponse> tasks = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    @GetMapping
    @Operation(summary = "List tasks", description = "Optionally filter by priority. Sorted by due date.")
    public List<TaskResponse> list(
            @Parameter(description = "Only tasks with this priority") @RequestParam(required = false) Priority priority) {
        return tasks.values().stream()
                .filter(t -> priority == null || t.priority() == priority)
                .sorted(Comparator.comparing(TaskResponse::dueOn))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one task")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "No task with this id",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public TaskResponse get(@Parameter(description = "Task id", example = "1") @PathVariable long id) {
        TaskResponse task = tasks.get(id);
        if (task == null) {
            throw new NoSuchElementException("task " + id + " not found");
        }
        return task;
    }

    @PostMapping
    @Operation(summary = "Create a task")
    @ApiResponse(responseCode = "201", description = "Created; Location header points to the new task")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        long id = ids.incrementAndGet();
        Priority priority = request.priority() == null ? Priority.MEDIUM : request.priority();
        TaskResponse task = new TaskResponse(id, request.title(), priority, request.dueOn(), false);
        tasks.put(id, task);
        return ResponseEntity.created(URI.create("/api/tasks/" + id)).body(task);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Requires a bearer token (documented security requirement).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        tasks.remove(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail notFound(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
