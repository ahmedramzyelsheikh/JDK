import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Invariance vs array covariance, wildcards and PECS, type erasure and its consequences,
 * bridge methods, raw types / heap pollution, and type tokens.
 * Run: java WildcardsAndErasure.java
 */
public class WildcardsAndErasure {

    // ---------- PECS ----------
    static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (T item : src) {         // read from the producer
            dest.add(item);          // write into the consumer
        }
    }

    static double sumAll(List<? extends Number> numbers) {      // producer: read only
        double total = 0;
        for (Number n : numbers) {
            total += n.doubleValue();
        }
        // numbers.add(1);                                      // compile error: which subtype?
        return total;
    }

    static void addZeros(List<? super Integer> sink, int n) {   // consumer: write Integers
        for (int i = 0; i < n; i++) {
            sink.add(0);
        }
        Object o = sink.get(0);                                 // can only read as Object
    }

    static int countAnything(List<?> anything) {                // unknown type: read as Object
        return anything.size();
    }

    // ---------- erasure work-arounds ----------
    static <T> List<T> createMany(Supplier<T> factory, int n) {  // instead of new T()
        List<T> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(factory.get());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    static <T> T[] newArray(Class<T> type, int size) {           // instead of new T[size]
        return (T[]) java.lang.reflect.Array.newInstance(type, size);
    }

    // ---------- bridge method ----------
    record Person(String name) implements Comparable<Person> {
        @Override
        public int compareTo(Person other) {
            return name.compareTo(other.name);
        }
    }

    // ---------- type token (how Jackson's TypeReference works) ----------
    abstract static class TypeToken<T> {
        final Type type;
        protected TypeToken() {
            this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
    }

    static class Library {
        List<String> titles = new ArrayList<>();                 // generic field signature is kept
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) throws Exception {
        System.out.println("=== Invariance vs covariance ===");
        List<Integer> ints = new ArrayList<>(List.of(1, 2, 3));
        // List<Number> nums = ints;                             // compile error: generics are invariant
        Object[] objects = new Integer[1];                       // arrays are covariant
        try {
            objects[0] = "boom";
        } catch (ArrayStoreException e) {
            System.out.println("  array covariance fails at RUNTIME: ArrayStoreException");
        }

        System.out.println("\n=== Wildcards / PECS ===");
        List<Number> numbers = new ArrayList<>();
        copy(numbers, ints);                                     // T = Integer, dest is List<? super Integer>
        copy(numbers, List.of(2.5, 3.5));                        // T = Double
        System.out.println("  copy into List<Number>: " + numbers);
        System.out.println("  sumAll(List<Integer>) = " + sumAll(ints) + ", sumAll(List<Double>) = " + sumAll(List.of(0.5, 0.25)));
        List<Object> sink = new ArrayList<>();
        addZeros(sink, 3);
        System.out.println("  addZeros(List<Object>) = " + sink + ", countAnything = " + countAnything(sink));
        List<String> words = new ArrayList<>(List.of("bb", "a", "ccc"));
        Comparator<Object> byToStringLength = Comparator.comparingInt(o -> o.toString().length());
        words.sort(byToStringLength);                            // Comparator<? super String> accepts Comparator<Object>
        System.out.println("  sort List<String> with Comparator<Object>: " + words);

        System.out.println("\n=== Type erasure ===");
        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();
        System.out.println("  same runtime class? " + (strings.getClass() == integers.getClass()) + " -> " + strings.getClass().getName());
        System.out.println("  instanceof List<?> works: " + (strings instanceof List<?>));
        System.out.println("  createMany(StringBuilder::new, 2): " + createMany(StringBuilder::new, 2).size() + " objects");
        String[] arr = newArray(String.class, 3);
        System.out.println("  newArray(String.class, 3): " + arr.getClass().getSimpleName() + " " + Arrays.toString(arr));

        System.out.println("\n=== Raw types & heap pollution ===");
        List raw = strings;                                      // raw type: no checks
        raw.add(42);                                             // compiles (unchecked warning)
        try {
            String s = strings.get(0);                           // cast inserted by javac fails HERE
            System.out.println(s);
        } catch (ClassCastException e) {
            System.out.println("  Integer hidden in List<String> -> ClassCastException far from the cause");
        }

        System.out.println("\n=== Bridge method ===");
        for (Method m : Person.class.getDeclaredMethods()) {
            if (m.getName().equals("compareTo")) {
                System.out.println("  compareTo(" + m.getParameterTypes()[0].getSimpleName() + ") bridge=" + m.isBridge());
            }
        }

        System.out.println("\n=== Generic info that survives erasure ===");
        Type fieldType = Library.class.getDeclaredField("titles").getGenericType();
        System.out.println("  field signature: " + fieldType);
        Type token = new TypeToken<List<Person>>() { }.type;     // anonymous subclass captures the type
        System.out.println("  type token: " + token);
    }
}
