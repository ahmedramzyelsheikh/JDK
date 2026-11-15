package com.jdk.mvc.task;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * DATA LAYER: in-memory storage. Thread-safe because one bean instance serves all requests.
 * Replace with Spring Data JPA after topic 10 - the service and controller won't change.
 */
@Repository
public class TaskRepository {

    private final Map<Long, Task> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Task save(Task task) {
        Task toSave = task.id() == 0
                ? new Task(sequence.incrementAndGet(), task.title(), task.priority(), task.done(), task.createdAt())
                : task;
        store.put(toSave.id(), toSave);
        return toSave;
    }

    public Optional<Task> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Task> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Task::done)
                        .thenComparing(Task::priority, Comparator.reverseOrder())
                        .thenComparing(Task::id))
                .toList();
    }

    public boolean existsByTitleIgnoreCase(String title) {
        return store.values().stream().anyMatch(t -> t.title().equalsIgnoreCase(title.strip()));
    }

    public void deleteById(long id) {
        store.remove(id);
    }
}
