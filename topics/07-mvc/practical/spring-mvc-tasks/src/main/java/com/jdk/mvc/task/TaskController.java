package com.jdk.mvc.task;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CONTROLLER: translates HTTP into service calls and chooses the view. Thin and stateless.
 */
@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    /** GET /tasks -> render templates/tasks.html with the list and an empty form. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("tasks", service.findAll());
        if (!model.containsAttribute("taskForm")) {
            model.addAttribute("taskForm", new TaskForm());
        }
        return "tasks";                                            // logical view name
    }

    /** POST /tasks -> validate, create, then redirect (Post/Redirect/Get). */
    @PostMapping
    public String create(@Valid TaskForm taskForm, BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            model.addAttribute("tasks", service.findAll());
            return "tasks";                                        // re-render the page WITH the errors, no redirect
        }
        try {
            Task created = service.create(taskForm);
            redirect.addFlashAttribute("message", "Task '" + created.title() + "' created");
        } catch (TaskService.DuplicateTaskException e) {
            errors.rejectValue("title", "duplicate", e.getMessage());
            model.addAttribute("tasks", service.findAll());
            return "tasks";
        }
        return "redirect:/tasks";                                  // 302 -> browser does GET /tasks
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable long id, RedirectAttributes redirect) {
        Task task = service.toggle(id);
        redirect.addFlashAttribute("message", "Task '" + task.title() + "' marked " + (task.done() ? "done" : "open"));
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("message", "Task #" + id + " deleted");
        return "redirect:/tasks";
    }
}
