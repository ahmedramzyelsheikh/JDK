import java.util.List;

/**
 * extends, super, constructor rules, field hiding, static method hiding, protected, final.
 * Run: java Inheritance.java
 */
public class Inheritance {

    static class Employee {
        protected final String name;
        protected final double baseSalary;
        String type = "Employee";                       // will be HIDDEN in Manager

        Employee(String name, double baseSalary) {       // no no-arg constructor!
            this.name = name;
            this.baseSalary = baseSalary;
        }

        double salary() {
            return baseSalary;
        }

        static String category() {
            return "Employee.category()";
        }

        final String badge() {                          // cannot be overridden
            return "[" + name + "]";
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + name + " -> " + salary();
        }
    }

    static class Manager extends Employee {
        private final double bonus;
        String type = "Manager";                        // hides Employee.type (fields are not polymorphic)

        Manager(String name, double base, double bonus) {
            super(name, base);                          // REQUIRED: parent has no no-arg constructor
            this.bonus = bonus;
        }

        @Override
        double salary() {
            return super.salary() + bonus;              // reuse parent logic
        }

        static String category() {                      // HIDES, does not override
            return "Manager.category()";
        }

        // String badge() { ... }                       // compile error: badge() is final
    }

    static class Engineer extends Employee {
        Engineer(String name, double base) {
            super(name, base);
        }

        @Override
        double salary() {
            return baseSalary * 1.2;
        }
    }

    static final class Intern extends Employee {        // final: no class can extend Intern
        Intern(String name) {
            super(name, 2_000);
        }
    }

    public static void main(String[] args) {
        List<Employee> staff = List.of(
                new Employee("Ali", 5_000),
                new Manager("Sara", 10_000, 3_000),
                new Engineer("Omar", 8_000),
                new Intern("Mona"));
        staff.forEach(System.out::println);               // polymorphic salary()

        Employee ref = new Manager("Hany", 9_000, 1_000);
        System.out.println("\n=== Hiding (resolved by the REFERENCE type) ===");
        System.out.println("ref.type (field)        = " + ref.type + "   <- Employee's field");
        System.out.println("((Manager) ref).type    = " + ((Manager) ref).type);
        System.out.println("Employee.category()     = " + Employee.category());
        System.out.println("Manager.category()      = " + Manager.category());
        System.out.println("ref.salary() (override) = " + ref.salary() + "   <- Manager's method");
        System.out.println("final badge()           = " + ref.badge());

        System.out.println("\n=== Is-a checks ===");
        System.out.println("Manager is an Employee? " + (ref instanceof Employee));
        System.out.println("Manager is an Object?   " + (ref instanceof Object));
        System.out.println("superclass of Manager:  " + Manager.class.getSuperclass().getSimpleName());
        System.out.println("superclass of Employee: " + Employee.class.getSuperclass().getSimpleName());
    }
}
