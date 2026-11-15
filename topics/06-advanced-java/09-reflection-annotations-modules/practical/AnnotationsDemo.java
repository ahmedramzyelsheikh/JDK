import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom runtime annotations processed by reflection, like Bean Validation and Spring MVC do:
 * a mini validator (@NotBlank, @Range) and a mini router (@Get on controller methods).
 * Run: java AnnotationsDemo.java
 */
public class AnnotationsDemo {

    // ---------- validation annotations ----------
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD})
    @interface NotBlank {
        String message() default "must not be blank";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD})
    @interface Range {
        int min() default 0;
        int max();
        String message() default "out of range";
    }

    // annotation WITHOUT runtime retention: invisible to reflection
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.RECORD_COMPONENT)
    @interface Invisible { }

    record CreateUserRequest(
            @NotBlank String username,
            @NotBlank(message = "email is required") String email,
            @Range(min = 18, max = 120, message = "age must be 18-120") @Invisible int age) { }

    static List<String> validate(Record object) throws ReflectiveOperationException {
        List<String> errors = new ArrayList<>();
        for (RecordComponent c : object.getClass().getRecordComponents()) {
            Object value = c.getAccessor().invoke(object);
            NotBlank notBlank = c.getAnnotation(NotBlank.class);
            if (notBlank != null && (value == null || value.toString().isBlank())) {
                errors.add(c.getName() + ": " + notBlank.message());
            }
            Range range = c.getAnnotation(Range.class);
            if (range != null && value instanceof Integer n && (n < range.min() || n > range.max())) {
                errors.add(c.getName() + ": " + range.message() + " (was " + n + ")");
            }
        }
        return errors;
    }

    // ---------- routing annotations ----------
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Controller {
        String value();                       // element named "value" can be written without a name
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Get {
        String value();
    }

    @Controller("/users")
    static class UserController {
        @Get("/list")
        public String list() { return "[Ali, Sara]"; }

        @Get("/count")
        public String count() { return "2"; }

        public String notAnEndpoint() { return "ignored"; }
    }

    static Map<String, Runnable> buildRoutes(Object... controllers) {
        Map<String, Runnable> routes = new HashMap<>();
        for (Object controller : controllers) {
            Controller base = controller.getClass().getAnnotation(Controller.class);
            for (Method m : controller.getClass().getDeclaredMethods()) {
                Get get = m.getAnnotation(Get.class);
                if (base != null && get != null) {
                    routes.put("GET " + base.value() + get.value(), () -> {
                        try {
                            System.out.println("    -> " + m.invoke(controller));
                        } catch (ReflectiveOperationException e) {
                            throw new IllegalStateException(e);
                        }
                    });
                }
            }
        }
        return routes;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Mini Bean Validation ===");
        for (CreateUserRequest req : List.of(
                new CreateUserRequest("ali", "ali@x.com", 25),
                new CreateUserRequest(" ", null, 12))) {
            List<String> errors = validate(req);
            System.out.println("  " + req + " -> " + (errors.isEmpty() ? "valid" : errors));
        }
        RecordComponent age = CreateUserRequest.class.getRecordComponents()[2];
        System.out.println("  @Invisible visible at runtime? " + (age.getAnnotation(Invisible.class) != null) + " (RetentionPolicy.CLASS)");

        System.out.println("\n=== Mini router (what @RestController/@GetMapping do) ===");
        Map<String, Runnable> routes = buildRoutes(new UserController());
        System.out.println("  registered: " + routes.keySet());
        for (String request : List.of("GET /users/list", "GET /users/count", "GET /users/unknown")) {
            System.out.println("  " + request);
            routes.getOrDefault(request, () -> System.out.println("    -> 404 Not Found")).run();
        }
    }
}
