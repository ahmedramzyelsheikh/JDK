import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Inspecting classes, creating objects, invoking methods, private fields, records (a mini JSON writer),
 * strong encapsulation, and Method.invoke vs MethodHandle vs direct call.
 * Run: java ReflectionDemo.java
 */
public class ReflectionDemo {

    static class Employee {
        private final String name;
        private double salary;
        public static int created;

        public Employee(String name, double salary) {
            this.name = name;
            this.salary = salary;
            created++;
        }

        public String greet(String other) {
            return "Hi " + other + ", I'm " + name;
        }

        private void secretRaise(double percent) {
            salary *= 1 + percent / 100;
        }

        public double getSalary() {
            return salary;
        }
    }

    record Product(String name, double price, List<String> tags) { }

    public static void main(String[] args) throws Throwable {
        System.out.println("=== Inspect ===");
        Class<?> type = Class.forName("ReflectionDemo$Employee");
        System.out.println("  class " + type.getSimpleName() + " extends " + type.getSuperclass().getSimpleName());
        for (Field f : type.getDeclaredFields()) {
            System.out.println("  field  " + Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName() + " " + f.getName());
        }
        for (Method m : type.getDeclaredMethods()) {
            System.out.println("  method " + Modifier.toString(m.getModifiers()) + " " + m.getName() + paramList(m.getParameterTypes()));
        }

        System.out.println("\n=== Create and invoke ===");
        Constructor<?> ctor = type.getConstructor(String.class, double.class);
        Object emp = ctor.newInstance("Ali", 10_000);
        Method greet = type.getMethod("greet", String.class);
        System.out.println("  " + greet.invoke(emp, "Sara"));

        System.out.println("\n=== Private members ===");
        Method raise = type.getDeclaredMethod("secretRaise", double.class);
        raise.setAccessible(true);                                     // bypass 'private'
        raise.invoke(emp, 10);
        Field salary = type.getDeclaredField("salary");
        salary.setAccessible(true);
        System.out.println("  salary after private secretRaise(10): " + salary.get(emp));
        Field name = type.getDeclaredField("name");
        name.setAccessible(true);
        name.set(emp, "Changed");                                      // even final instance fields (don't!)
        System.out.println("  final field changed via reflection: " + greet.invoke(emp, "you"));
        System.out.println("  static field via reflection: created = " + type.getField("created").get(null));

        System.out.println("\n=== Records: a 15-line JSON writer ===");
        System.out.println("  " + toJson(new Product("Laptop", 999.5, List.of("tech", "sale"))));

        System.out.println("\n=== Strong encapsulation of the JDK ===");
        try {
            Field value = String.class.getDeclaredField("value");
            value.setAccessible(true);
        } catch (InaccessibleObjectException e) {
            System.out.println("  String.value is not open: " + e.getClass().getSimpleName());
        }

        System.out.println("\n=== Performance: direct vs Method.invoke vs MethodHandle (10M calls) ===");
        Employee direct = new Employee("Perf", 1);
        Method getter = Employee.class.getMethod("getSalary");
        MethodHandle handle = MethodHandles.lookup().findVirtual(Employee.class, "getSalary", MethodType.methodType(double.class));
        for (int round = 0; round < 2; round++) {                       // round 0 = warm-up
            long t0 = System.nanoTime();
            double s1 = 0;
            for (int i = 0; i < 10_000_000; i++) s1 += direct.getSalary();
            long t1 = System.nanoTime();
            double s2 = 0;
            for (int i = 0; i < 10_000_000; i++) s2 += (double) getter.invoke(direct);
            long t2 = System.nanoTime();
            double s3 = 0;
            for (int i = 0; i < 10_000_000; i++) s3 += (double) handle.invokeExact(direct);
            long t3 = System.nanoTime();
            if (round == 1) {
                System.out.printf("  direct %d ms, Method.invoke %d ms, MethodHandle %d ms (checksums %.0f %.0f %.0f)%n",
                        (t1 - t0) / 1_000_000, (t2 - t1) / 1_000_000, (t3 - t2) / 1_000_000, s1, s2, s3);
            }
        }
    }

    static String toJson(Record r) throws ReflectiveOperationException {
        StringJoiner json = new StringJoiner(", ", "{", "}");
        for (RecordComponent c : r.getClass().getRecordComponents()) {
            Object v = c.getAccessor().invoke(r);
            String value = switch (v) {
                case String s -> "\"" + s + "\"";
                case List<?> list -> {
                    List<String> items = new ArrayList<>();
                    list.forEach(x -> items.add("\"" + x + "\""));
                    yield "[" + String.join(", ", items) + "]";
                }
                default -> String.valueOf(v);
            };
            json.add("\"" + c.getName() + "\": " + value);
        }
        return json.toString();
    }

    static String paramList(Class<?>[] params) {
        StringJoiner j = new StringJoiner(", ", "(", ")");
        for (Class<?> p : params) {
            j.add(p.getSimpleName());
        }
        return j.toString();
    }
}
