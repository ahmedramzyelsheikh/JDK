import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A REST API with ZERO frameworks: the JDK's built-in HttpServer as server and java.net.http.HttpClient
 * as client. It starts the server on a free port, sends real HTTP requests, prints them, then stops.
 * JSON is built by hand here on purpose - Spring + Jackson do this for you (see spring-rest-products).
 * Run: java JdkRestServerAndClient.java
 */
public class JdkRestServerAndClient {

    record Book(long id, String title, String author) {
        String toJson() {
            return "{\"id\":%d,\"title\":\"%s\",\"author\":\"%s\"}".formatted(id, escape(title), escape(author));
        }
    }

    static final Map<Long, Book> BOOKS = new ConcurrentHashMap<>();
    static final AtomicLong IDS = new AtomicLong();
    static final Pattern ITEM = Pattern.compile("^/api/books/(\\d+)$");
    static final Pattern FIELD = Pattern.compile("\"(title|author)\"\\s*:\\s*\"([^\"]*)\"");

    // ============================ SERVER ============================
    static void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        try {
            if (path.equals("/api/books")) {
                switch (method) {
                    case "GET" -> send(ex, 200, BOOKS.values().stream().sorted((a, b) -> Long.compare(a.id(), b.id()))
                            .map(Book::toJson).collect(Collectors.joining(",", "[", "]")));
                    case "POST" -> {
                        Map<String, String> body = parse(ex);
                        if (body.getOrDefault("title", "").isBlank()) {
                            problem(ex, 400, "Validation failed", "title must not be blank");
                            return;
                        }
                        Book book = new Book(IDS.incrementAndGet(), body.get("title"), body.getOrDefault("author", "unknown"));
                        BOOKS.put(book.id(), book);
                        ex.getResponseHeaders().add("Location", "/api/books/" + book.id());
                        send(ex, 201, book.toJson());
                    }
                    default -> methodNotAllowed(ex, "GET, POST");
                }
                return;
            }
            Matcher m = ITEM.matcher(path);
            if (m.matches()) {
                long id = Long.parseLong(m.group(1));
                Book existing = BOOKS.get(id);
                if (existing == null) {
                    problem(ex, 404, "Not Found", "book " + id + " does not exist");
                    return;
                }
                switch (method) {
                    case "GET" -> send(ex, 200, existing.toJson());
                    case "PUT" -> {
                        Map<String, String> body = parse(ex);
                        Book updated = new Book(id, body.getOrDefault("title", ""), body.getOrDefault("author", ""));
                        BOOKS.put(id, updated);
                        send(ex, 200, updated.toJson());
                    }
                    case "DELETE" -> {
                        BOOKS.remove(id);
                        send(ex, 204, null);
                    }
                    default -> methodNotAllowed(ex, "GET, PUT, DELETE");
                }
                return;
            }
            problem(ex, 404, "Not Found", "no route for " + path);
        } catch (RuntimeException e) {
            problem(ex, 500, "Internal Server Error", "unexpected error");    // never leak the stack trace
        }
    }

    static Map<String, String> parse(HttpExchange ex) throws IOException {
        String json = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> fields = new TreeMap<>();
        Matcher m = FIELD.matcher(json);
        while (m.find()) {
            fields.put(m.group(1), m.group(2));
        }
        return fields;
    }

    static void methodNotAllowed(HttpExchange ex, String allow) throws IOException {
        ex.getResponseHeaders().add("Allow", allow);
        problem(ex, 405, "Method Not Allowed", ex.getRequestMethod() + " is not supported here");
    }

    static void problem(HttpExchange ex, int status, String title, String detail) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/problem+json");
        send(ex, status, "{\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}".formatted(title, status, escape(detail)), false);
    }

    static void send(HttpExchange ex, int status, String body) throws IOException {
        send(ex, status, body, true);
    }

    static void send(HttpExchange ex, int status, String body, boolean json) throws IOException {
        if (json && body != null) {
            ex.getResponseHeaders().set("Content-Type", "application/json");
        }
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, body == null ? -1 : bytes.length);    // -1 = no body (204)
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ============================ CLIENT ============================
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);   // port 0 = any free port
        server.createContext("/", JdkRestServerAndClient::handle);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());                     // one virtual thread per request
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();
        System.out.println("Server listening on " + base + "\n");

        HttpClient client = HttpClient.newHttpClient();
        try {
            call(client, "POST", base + "/api/books", "{\"title\":\"Effective Java\",\"author\":\"Joshua Bloch\"}");
            call(client, "POST", base + "/api/books", "{\"title\":\"Clean Code\",\"author\":\"Robert Martin\"}");
            call(client, "POST", base + "/api/books", "{\"title\":\"\"}");                      // 400
            call(client, "GET", base + "/api/books", null);
            call(client, "GET", base + "/api/books/1", null);
            call(client, "PUT", base + "/api/books/2", "{\"title\":\"Clean Code (2nd ed.)\",\"author\":\"Robert C. Martin\"}");
            call(client, "DELETE", base + "/api/books/1", null);                                 // 204
            call(client, "GET", base + "/api/books/1", null);                                    // 404
            call(client, "PATCH", base + "/api/books", null);                                    // 405
            call(client, "GET", base + "/api/authors", null);                                    // 404 no route
        } finally {
            server.stop(0);
            System.out.println("Server stopped.");
        }
    }

    static void call(HttpClient client, String method, String url, String json) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).header("Accept", "application/json");
        if (json != null) {
            builder.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(json));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        System.out.println("> " + method + " " + URI.create(url).getPath() + (json != null ? "  " + json : ""));
        System.out.print("< " + response.statusCode());
        response.headers().firstValue("Location").ifPresent(l -> System.out.print("  Location: " + l));
        response.headers().firstValue("Allow").ifPresent(a -> System.out.print("  Allow: " + a));
        response.headers().firstValue("Content-Type").ifPresent(t -> System.out.print("  [" + t + "]"));
        System.out.println(response.body().isEmpty() ? "  (no body)" : "\n  " + response.body());
        System.out.println();
    }
}
