package com.jdk.mvc.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form-backing DTO: what the HTML form sends. Separate from the domain model so the
 * form can't set fields it shouldn't (id, done, createdAt) - protection against mass assignment.
 */
public class TaskForm {

    @NotBlank(message = "Title is required")
    @Size(max = 60, message = "Title must be at most 60 characters")
    private String title;

    @NotNull(message = "Choose a priority")
    private Task.Priority priority = Task.Priority.MEDIUM;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Task.Priority getPriority() {
        return priority;
    }

    public void setPriority(Task.Priority priority) {
        this.priority = priority;
    }
}
