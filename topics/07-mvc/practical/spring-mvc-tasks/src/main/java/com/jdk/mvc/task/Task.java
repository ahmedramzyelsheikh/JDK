package com.jdk.mvc.task;

import java.time.LocalDateTime;

/** MODEL: an immutable domain object. "Changing" a task creates a new one. */
public record Task(long id, String title, Priority priority, boolean done, LocalDateTime createdAt) {

    public enum Priority { LOW, MEDIUM, HIGH }

    public Task toggle() {
        return new Task(id, title, priority, !done, createdAt);
    }
}
