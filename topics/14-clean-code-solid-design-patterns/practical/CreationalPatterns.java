import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Singleton (enum), Factory Method, Abstract Factory, Builder, Prototype.
 * Run: java CreationalPatterns.java
 */
public class CreationalPatterns {

    // ---------- Singleton: an enum is serialization- and reflection-safe ----------
    enum AppConfig {
        INSTANCE;
        private final Map<String, String> values = Map.of("currency", "EGP", "env", "dev");
        String get(String key) { return values.get(key); }
    }

    // ---------- Factory Method: one place decides which implementation to create ----------
    interface Notifier { String send(String msg); }
    record EmailNotifier(String to) implements Notifier { public String send(String m) { return "EMAIL to " + to + ": " + m; } }
    record SmsNotifier(String phone) implements Notifier { public String send(String m) { return "SMS to " + phone + ": " + m; } }

    static Notifier notifierFor(String contact) {                       // static factory method
        return contact.contains("@") ? new EmailNotifier(contact) : new SmsNotifier(contact);
    }

    // ---------- Abstract Factory: families of related objects ----------
    interface Button { String render(); }
    interface Dialog { String render(); }
    interface UiFactory { Button button(String label); Dialog dialog(String title); }

    static class LightTheme implements UiFactory {
        public Button button(String l) { return () -> "[ " + l + " ] (light)"; }
        public Dialog dialog(String t) { return () -> "+-- " + t + " --+ (light)"; }
    }

    static class DarkTheme implements UiFactory {
        public Button button(String l) { return () -> "[# " + l + " #] (dark)"; }
        public Dialog dialog(String t) { return () -> "#== " + t + " ==# (dark)"; }
    }

    // ---------- Builder: many optional parameters, immutable result, validation in build() ----------
    static final class HttpCall {
        private final String method, url, body;
        private final Map<String, String> headers;
        private final int timeoutSeconds;

        private HttpCall(Builder b) {
            method = b.method; url = b.url; body = b.body; headers = Map.copyOf(b.headers); timeoutSeconds = b.timeoutSeconds;
        }

        static Builder builder(String url) { return new Builder(url); }

        @Override public String toString() {
            return method + " " + url + " headers=" + headers + " timeout=" + timeoutSeconds + "s" + (body != null ? " body=" + body : "");
        }

        static final class Builder {
            private final String url;
            private String method = "GET", body;
            private final Map<String, String> headers = new java.util.TreeMap<>();
            private int timeoutSeconds = 30;

            private Builder(String url) { this.url = Objects.requireNonNull(url); }
            Builder post(String json) { method = "POST"; body = json; return this; }
            Builder header(String k, String v) { headers.put(k, v); return this; }
            Builder timeout(int s) { timeoutSeconds = s; return this; }

            HttpCall build() {
                if (body != null && !headers.containsKey("Content-Type")) {
                    throw new IllegalStateException("POST with body needs a Content-Type header");
                }
                return new HttpCall(this);
            }
        }
    }

    // ---------- Prototype: create new objects by copying a configured one ----------
    record Document(String template, List<String> sections) {
        Document copyWith(String extra) {
            List<String> copy = new ArrayList<>(sections);   // deep enough copy
            copy.add(extra);
            return new Document(template, copy);
        }
    }

    public static void main(String[] args) {
        System.out.println("Singleton:        currency=" + AppConfig.INSTANCE.get("currency")
                + ", same instance=" + (AppConfig.INSTANCE == AppConfig.valueOf("INSTANCE")));

        for (String contact : List.of("ali@x.com", "+201001234567")) {
            System.out.println("Factory Method:   " + notifierFor(contact).send("your order shipped"));
        }

        for (UiFactory theme : List.of(new LightTheme(), new DarkTheme())) {
            System.out.println("Abstract Factory: " + theme.dialog("Confirm").render() + "  " + theme.button("OK").render());
        }

        System.out.println("Builder:          " + HttpCall.builder("https://api.shop/orders")
                .post("{\"sku\":\"pen\"}").header("Content-Type", "application/json").header("Authorization", "Bearer x").timeout(5).build());
        try {
            HttpCall.builder("https://api.shop/orders").post("{}").build();
        } catch (IllegalStateException e) {
            System.out.println("Builder validates: " + e.getMessage());
        }

        Document invoiceTemplate = new Document("invoice", List.of("header", "lines", "totals"));
        Document withNotes = invoiceTemplate.copyWith("notes");
        System.out.println("Prototype:        original " + invoiceTemplate.sections() + ", copy " + withNotes.sections());

        Supplier<List<String>> factory = ArrayList::new;      // constructor reference = tiny factory
        System.out.println("Factory as lambda: " + factory.get().getClass().getSimpleName());
    }
}
