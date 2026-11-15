import java.util.ArrayList;
import java.util.List;

/**
 * MVC without a framework: the Model notifies its observers (classic Smalltalk MVC), the View renders
 * to the console, and the Controller turns user commands into model operations.
 * The "user" is a scripted list of commands so the program runs without input.
 * Run: java ConsoleMvc.java
 */
public class ConsoleMvc {

    // ======================= MODEL =======================
    record Task(int id, String title, boolean done) { }

    interface ModelListener {
        void modelChanged(List<Task> tasks, String event);
    }

    /** Holds state and business rules. Knows nothing about the console or commands. */
    static class TaskModel {
        private final List<Task> tasks = new ArrayList<>();
        private final List<ModelListener> listeners = new ArrayList<>();
        private int nextId = 1;

        void addListener(ModelListener l) {
            listeners.add(l);
        }

        void add(String title) {
            if (title.isBlank()) {
                throw new IllegalArgumentException("title must not be blank");
            }
            boolean duplicate = tasks.stream().anyMatch(t -> t.title().equalsIgnoreCase(title));
            if (duplicate) {
                throw new IllegalArgumentException("task '" + title + "' already exists");   // business rule
            }
            tasks.add(new Task(nextId++, title, false));
            notifyListeners("added '" + title + "'");
        }

        void toggle(int id) {
            int index = indexOf(id);
            Task t = tasks.get(index);
            tasks.set(index, new Task(t.id(), t.title(), !t.done()));
            notifyListeners("toggled #" + id);
        }

        void remove(int id) {
            tasks.remove(indexOf(id));
            notifyListeners("removed #" + id);
        }

        private int indexOf(int id) {
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).id() == id) {
                    return i;
                }
            }
            throw new IllegalArgumentException("no task #" + id);
        }

        private void notifyListeners(String event) {
            List<Task> snapshot = List.copyOf(tasks);
            listeners.forEach(l -> l.modelChanged(snapshot, event));
        }
    }

    // ======================= VIEWS =======================
    /** Renders the full list whenever the model changes. */
    static class ConsoleView implements ModelListener {
        @Override
        public void modelChanged(List<Task> tasks, String event) {
            String line = "  +" + "-".repeat(34) + "+";
            System.out.println(line);
            System.out.printf("  | %-32s |%n", "event: " + event);
            for (Task t : tasks) {
                System.out.printf("  | %s #%-2d %-24s |%n", t.done() ? "[x]" : "[ ]", t.id(), t.title());
            }
            System.out.println(line);
        }

        void showError(String message) {
            System.out.println("  !! " + message);
        }
    }

    /** A second view on the same model: just statistics. */
    static class StatsView implements ModelListener {
        @Override
        public void modelChanged(List<Task> tasks, String event) {
            long done = tasks.stream().filter(Task::done).count();
            System.out.println("  stats: " + done + "/" + tasks.size() + " done");
        }
    }

    // ===================== CONTROLLER =====================
    /** Parses input and calls the model. No business rules, no rendering. */
    static class TaskController {
        private final TaskModel model;
        private final ConsoleView view;

        TaskController(TaskModel model, ConsoleView view) {
            this.model = model;
            this.view = view;
        }

        void handle(String command) {
            System.out.println("> " + command);
            String[] parts = command.split(" ", 2);
            try {
                switch (parts[0]) {
                    case "add" -> model.add(parts.length > 1 ? parts[1] : "");
                    case "done" -> model.toggle(Integer.parseInt(parts[1]));
                    case "rm" -> model.remove(Integer.parseInt(parts[1]));
                    default -> view.showError("unknown command: " + parts[0]);
                }
            } catch (IllegalArgumentException e) {      // includes NumberFormatException
                view.showError(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        TaskModel model = new TaskModel();
        ConsoleView view = new ConsoleView();
        model.addListener(view);
        model.addListener(new StatsView());
        TaskController controller = new TaskController(model, view);

        List<String> userInput = List.of(
                "add Learn MVC",
                "add Build Spring app",
                "add learn mvc",          // duplicate -> business rule in the model
                "done 1",
                "done x",                 // bad input -> controller handles
                "rm 2",
                "fly");                   // unknown command
        userInput.forEach(controller::handle);
    }
}
