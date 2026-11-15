import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Creating Optionals, transforming them, defaults, orElse vs orElseGet, and anti-patterns.
 * Run: java OptionalDemo.java
 */
public class OptionalDemo {

    record Address(String city) { }
    record User(String email, String name, Address address) { }

    static final Map<String, User> USERS = Map.of(
            "ali@x.com", new User("ali@x.com", "Ali", new Address("Cairo")),
            "sara@x.com", new User("sara@x.com", "Sara", null));

    static Optional<User> findByEmail(String email) {
        return Optional.ofNullable(USERS.get(email));          // never return null from here
    }

    static Optional<Address> address(User u) {
        return Optional.ofNullable(u.address());
    }

    static String expensiveDefault() {
        System.out.println("    (expensiveDefault() was called)");
        return "default";
    }

    public static void main(String[] args) {
        System.out.println("=== Creation ===");
        System.out.println("  of(\"x\")=" + Optional.of("x") + ", ofNullable(null)=" + Optional.ofNullable(null) + ", empty()=" + Optional.empty());
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("  Optional.of(null) -> NullPointerException (use ofNullable)");
        }

        System.out.println("\n=== map / flatMap / filter / orElse ===");
        for (String email : List.of("ali@x.com", "sara@x.com", "nobody@x.com")) {
            String city = findByEmail(email)
                    .flatMap(OptionalDemo::address)            // flatMap: function returns Optional
                    .map(Address::city)                         // map: function returns a value
                    .filter(c -> !c.isBlank())
                    .orElse("unknown city");
            System.out.println("  " + email + " -> " + city);
        }

        System.out.println("\n=== orElse vs orElseGet ===");
        Optional<String> present = Optional.of("value");
        System.out.println("  orElse:    " + present.orElse(expensiveDefault()) + "  <- default computed anyway!");
        System.out.println("  orElseGet: " + present.orElseGet(OptionalDemo::expensiveDefault) + "  <- not computed");

        System.out.println("\n=== orElseThrow / ifPresentOrElse / or / stream ===");
        try {
            findByEmail("nobody@x.com").orElseThrow(() -> new NoSuchElementException("user nobody@x.com not found"));
        } catch (NoSuchElementException e) {
            System.out.println("  orElseThrow -> " + e.getMessage());
        }
        findByEmail("ali@x.com").ifPresentOrElse(
                u -> System.out.println("  welcome " + u.name()),
                () -> System.out.println("  no user"));
        Optional<User> fallback = findByEmail("nobody@x.com").or(() -> findByEmail("sara@x.com"));
        System.out.println("  or(...) fallback user: " + fallback.map(User::name).orElseThrow());
        List<String> found = List.of("ali@x.com", "nobody@x.com", "sara@x.com").stream()
                .map(OptionalDemo::findByEmail)
                .flatMap(Optional::stream)                     // drop empties (Java 9)
                .map(User::name)
                .toList();
        System.out.println("  Optional::stream keeps only present values: " + found);

        System.out.println("\n=== Anti-patterns ===");
        Optional<User> maybe = findByEmail("nobody@x.com");
        try {
            maybe.get();                                       // like calling a method on null
        } catch (NoSuchElementException e) {
            System.out.println("  get() on empty -> NoSuchElementException: prefer orElseThrow()/map/orElse");
        }
        if (maybe.isPresent()) {                               // works, but it's just a null check in disguise
            System.out.println(maybe.get().name());
        } else {
            System.out.println("  isPresent()+get() works but misses the point; use map/orElse");
        }
    }
}
