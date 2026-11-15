import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Generic classes, interfaces, methods, bounded types, multiple bounds and a generic repository.
 * Run: java GenericsBasics.java
 */
public class GenericsBasics {

    // ---------- generic class ----------
    record Pair<A, B>(A first, B second) {
        Pair<B, A> swap() {
            return new Pair<>(second, first);
        }
    }

    static class Box<T> {
        private final T value;
        Box(T value) { this.value = value; }
        T get() { return value; }
        <R> Box<R> map(Function<? super T, ? extends R> f) { return new Box<>(f.apply(value)); }
        @Override public String toString() { return "Box(" + value + ")"; }
    }

    // ---------- generic interface + implementation ----------
    interface Identifiable<ID> {
        ID id();
    }

    interface CrudRepository<T extends Identifiable<ID>, ID> {
        T save(T entity);
        Optional<T> findById(ID id);
        List<T> findAll();
    }

    static class InMemoryRepository<T extends Identifiable<ID>, ID> implements CrudRepository<T, ID> {
        private final Map<ID, T> store = new HashMap<>();
        @Override public T save(T entity) { store.put(entity.id(), entity); return entity; }
        @Override public Optional<T> findById(ID id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<T> findAll() { return new ArrayList<>(store.values()); }
    }

    record User(Long id, String name) implements Identifiable<Long> { }

    // ---------- generic methods ----------
    static <T> T firstOrDefault(List<T> list, T defaultValue) {
        return list.isEmpty() ? defaultValue : list.get(0);
    }

    static <T extends Number> double sum(List<T> numbers) {               // upper bound
        double total = 0;
        for (T n : numbers) {
            total += n.doubleValue();                                     // method of the bound
        }
        return total;
    }

    static <T extends Comparable<? super T>> T max(List<? extends T> items) {   // recursive bound
        T best = items.get(0);
        for (T item : items) {
            if (item.compareTo(best) > 0) {
                best = item;
            }
        }
        return best;
    }

    static <T extends Number & Comparable<T>> T clamp(T value, T min, T max) {  // multiple bounds
        if (value.compareTo(min) < 0) return min;
        if (value.compareTo(max) > 0) return max;
        return value;
    }

    public static void main(String[] args) {
        Pair<String, Integer> p = new Pair<>("age", 25);
        Pair<Integer, String> swapped = p.swap();
        System.out.println("Pair " + p + " swapped " + swapped);

        Box<String> name = new Box<>("generics");
        Box<Integer> length = name.map(String::length);
        System.out.println(name + " -> map(String::length) -> " + length);

        CrudRepository<User, Long> users = new InMemoryRepository<>();
        users.save(new User(1L, "Ali"));
        users.save(new User(2L, "Sara"));
        System.out.println("findById(2) = " + users.findById(2L).map(User::name).orElse("?")
                + ", findById(9) = " + users.findById(9L).map(User::name).orElse("not found")
                + ", count = " + users.findAll().size());

        System.out.println("firstOrDefault([], \"-\") = " + firstOrDefault(List.of(), "-"));
        System.out.println("sum of ints/doubles = " + sum(List.of(1, 2, 3)) + " / " + sum(List.of(1.5, 2.5)));
        System.out.println("max(strings) = " + max(List.of("pear", "apple", "zucchini")));
        System.out.println("max(dates) = " + max(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 11, 15))));
        System.out.println("clamp(150, 0, 100) = " + clamp(150, 0, 100) + ", clamp(-2.5, 0.0, 1.0) = " + clamp(-2.5, 0.0, 1.0));
        System.out.println("explicit type witness: " + GenericsBasics.<String>firstOrDefault(List.of("x"), "y"));
    }
}
