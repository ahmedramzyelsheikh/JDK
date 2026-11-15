package com.jdk.mvc.web;

import com.jdk.mvc.task.TaskService.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** One place that turns exceptions from any controller into an error page. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(TaskNotFoundException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "not-found";
    }
}
