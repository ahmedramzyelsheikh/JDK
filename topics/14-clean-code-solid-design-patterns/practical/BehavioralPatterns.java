import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Strategy, Observer, Template Method, Command (with undo), Chain of Responsibility, State.
 * Run: java BehavioralPatterns.java
 */
public class BehavioralPatterns {

    // ---------- Strategy: interchangeable algorithms chosen at runtime ----------
    interface ShippingStrategy { double cost(double weightKg); }

    static final Map<String, ShippingStrategy> SHIPPING = Map.of(       // Spring would inject Map<String, Bean>
            "standard", w -> 30 + 5 * w,
            "express", w -> 80 + 8 * w,
            "pickup", w -> 0);

    // ---------- Observer: publish events to subscribers ----------
    record OrderPlaced(String orderId, double total) { }

    static class EventBus {
        private final List<Consumer<OrderPlaced>> listeners = new ArrayList<>();
        void subscribe(Consumer<OrderPlaced> l) { listeners.add(l); }
        void publish(OrderPlaced e) { listeners.forEach(l -> l.accept(e)); }
    }

    // ---------- Template Method: fixed skeleton, steps overridden ----------
    abstract static class ReportExporter {
        final String export(List<String> rows) {                       // the template (final)
            StringBuilder sb = new StringBuilder(header());
            rows.forEach(r -> sb.append(row(r)));
            return sb.append(footer()).toString();
        }
        abstract String header();
        abstract String row(String r);
        String footer() { return ""; }                                  // hook with default
    }

    static class CsvExporter extends ReportExporter {
        String header() { return "name\n"; }
        String row(String r) { return r + "\n"; }
    }

    static class HtmlExporter extends ReportExporter {
        String header() { return "<ul>"; }
        String row(String r) { return "<li>" + r + "</li>"; }
        @Override String footer() { return "</ul>"; }
    }

    // ---------- Command: requests as objects -> history and undo ----------
    interface Command { void execute(); void undo(); String name(); }

    static class Cart {
        final List<String> items = new ArrayList<>();
    }

    record AddItem(Cart cart, String item) implements Command {
        public void execute() { cart.items.add(item); }
        public void undo() { cart.items.remove(item); }
        public String name() { return "add " + item; }
    }

    static class CommandRunner {
        private final Deque<Command> history = new ArrayDeque<>();
        void run(Command c) { c.execute(); history.push(c); }
        void undo() { if (!history.isEmpty()) history.pop().undo(); }
    }

    // ---------- Chain of Responsibility: like servlet filters ----------
    record Request(String user, String role, String path) { }

    interface Handler { String handle(Request r, Chain next); }

    static final class Chain {
        private final List<Handler> handlers;
        private final int index;
        Chain(List<Handler> handlers, int index) { this.handlers = handlers; this.index = index; }
        String proceed(Request r) {
            return index < handlers.size() ? handlers.get(index).handle(r, new Chain(handlers, index + 1)) : "200 OK " + r.path();
        }
    }

    // ---------- State: behaviour depends on the current state ----------
    enum OrderState {
        NEW { OrderState next(String event) { return event.equals("pay") ? PAID : event.equals("cancel") ? CANCELLED : invalid(event); } },
        PAID { OrderState next(String event) { return event.equals("ship") ? SHIPPED : event.equals("refund") ? CANCELLED : invalid(event); } },
        SHIPPED { OrderState next(String event) { return event.equals("deliver") ? DELIVERED : invalid(event); } },
        DELIVERED { OrderState next(String event) { return invalid(event); } },
        CANCELLED { OrderState next(String event) { return invalid(event); } };

        abstract OrderState next(String event);

        OrderState invalid(String event) {
            throw new IllegalStateException("cannot '" + event + "' an order that is " + this);
        }
    }

    public static void main(String[] args) {
        SHIPPING.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println("Strategy: " + e.getKey() + " shipping for 2kg = " + e.getValue().cost(2)));

        EventBus bus = new EventBus();
        bus.subscribe(e -> System.out.println("Observer: email receipt for " + e.orderId()));
        bus.subscribe(e -> System.out.println("Observer: add " + (int) (e.total() / 10) + " loyalty points"));
        bus.publish(new OrderPlaced("ORD-7", 350));

        List<String> rows = List.of("Ali", "Sara");
        System.out.print("Template: CSV\n" + new CsvExporter().export(rows));
        System.out.println("Template: HTML " + new HtmlExporter().export(rows));

        Cart cart = new Cart();
        CommandRunner runner = new CommandRunner();
        runner.run(new AddItem(cart, "book"));
        runner.run(new AddItem(cart, "pen"));
        runner.undo();
        System.out.println("Command:  after add book, add pen, undo -> " + cart.items);

        List<Handler> filters = List.of(
                (r, next) -> { System.out.println("Chain:    log " + r.path()); return next.proceed(r); },
                (r, next) -> r.user() == null ? "401 Unauthorized" : next.proceed(r),
                (r, next) -> r.path().startsWith("/admin") && !r.role().equals("ADMIN") ? "403 Forbidden" : next.proceed(r));
        System.out.println("Chain:    " + new Chain(filters, 0).proceed(new Request("ali", "USER", "/orders")));
        System.out.println("Chain:    " + new Chain(filters, 0).proceed(new Request("ali", "USER", "/admin/users")));
        System.out.println("Chain:    " + new Chain(filters, 0).proceed(new Request(null, "", "/orders")));

        OrderState state = OrderState.NEW;
        for (String event : List.of("pay", "ship", "deliver")) {
            state = state.next(event);
            System.out.println("State:    " + event + " -> " + state);
        }
        try {
            state.next("cancel");
        } catch (IllegalStateException e) {
            System.out.println("State:    " + e.getMessage());
        }
    }
}
