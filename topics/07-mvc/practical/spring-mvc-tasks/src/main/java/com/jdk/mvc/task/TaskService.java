package com.jdk.mvc.task;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SERVICE LAYER: use cases and business rules. Knows nothing about HTTP or HTML.
 * Dependencies come in through the constructor (constructor injection, no @Autowired needed).
 */
@Service
public class TaskService {

    private final TaskRepository repository;
    private final Clock clock;

    @Autowired      // needed because there are two constructors: tells Spring which one to use
    public TaskService(TaskRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    TaskService(TaskRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Task create(TaskForm form) {
        if (repository.existsByTitleIgnoreCase(form.getTitle())) {
            throw new DuplicateTaskException(form.getTitle());              // business rule
        }
        return repository.save(new Task(0, form.getTitle().strip(), form.getPriority(), false, LocalDateTime.now(clock)));
    }

    public Task toggle(long id) {
        Task task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        return repository.save(task.toggle());
    }

    public void delete(long id) {
        repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        repository.deleteById(id);
    }

    public static class TaskNotFoundException extends RuntimeException {
        public TaskNotFoundException(long id) {
            super("Task #" + id + " does not exist");
        }
    }

    public static class DuplicateTaskException extends RuntimeException {
        public DuplicateTaskException(String title) {
            super("A task called '" + title.strip() + "' already exists");
        }
    }
}
