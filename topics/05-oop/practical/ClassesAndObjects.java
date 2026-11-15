/**
 * Classes, objects, references, constructors, chaining, this, static, final and initialisation order.
 * Run: java ClassesAndObjects.java
 */
public class ClassesAndObjects {

    static class Car {
        static int carsCreated;                     // one copy for the class
        private final String model;                 // final: assigned once
        private int speed;

        Car(String model) {
            this.model = model;                     // this.field vs parameter
            carsCreated++;
        }

        Car accelerate(int delta) {
            speed += delta;
            return this;                            // fluent API
        }

        @Override
        public String toString() {
            return model + "@" + speed + "km/h";
        }
    }

    static class Employee {
        private final String name;
        private final double salary;

        Employee(String name) {
            this(name, 5_000);                      // constructor chaining
        }

        Employee(String name, double salary) {
            if (salary < 0) {
                throw new IllegalArgumentException("salary must be >= 0");
            }
            this.name = name;
            this.salary = salary;
        }

        @Override
        public String toString() {
            return name + " earns " + salary;
        }
    }

    static class NotAConstructor {
        String value = "field initialiser";

        void NotAConstructor() {                    // has a return type -> it's a METHOD
            value = "set by method";
        }
    }

    static class Base {
        static { log("Base static block"); }
        String baseField = log("Base instance field");
        { log("Base instance block"); }

        Base() {
            log("Base constructor");
            describe();                             // DANGER: overridable call in constructor
        }

        void describe() { }
    }

    static class Derived extends Base {
        static { log("Derived static block"); }
        private String name = log("Derived instance field");
        { log("Derived instance block"); }

        Derived() {
            super();                                // inserted automatically if omitted
            log("Derived constructor, name = " + name);
        }

        @Override
        void describe() {
            log("Derived.describe() called from Base constructor sees name = " + name + "  <- not initialised yet!");
        }
    }

    static String log(String message) {
        System.out.println("  " + message);
        return message;
    }

    public static void main(String[] args) {
        System.out.println("=== Objects and references ===");
        Car c1 = new Car("BMW");
        Car c2 = new Car("Kia");
        Car c3 = c1;                                // same object
        c3.accelerate(50).accelerate(20);
        System.out.println(c1 + " " + c2 + "  c1 == c3: " + (c1 == c3) + "  cars created: " + Car.carsCreated);

        System.out.println("\n=== Constructors ===");
        System.out.println(new Employee("Ali"));
        System.out.println(new Employee("Sara", 9_000));
        try {
            new Employee("Bad", -1);
        } catch (IllegalArgumentException e) {
            System.out.println("validation in constructor -> " + e.getMessage());
        }
        NotAConstructor n = new NotAConstructor();
        System.out.println("void NotAConstructor() is a method: value = " + n.value);

        System.out.println("\n=== Initialisation order (first object) ===");
        new Derived();
        System.out.println("=== Second object: static blocks don't run again ===");
        new Derived();

        final StringBuilder sb = new StringBuilder("final reference");
        sb.append(", mutable object");              // allowed: the object changes, not the reference
        // sb = new StringBuilder();                // compile error
        System.out.println("\n" + sb);
    }
}
