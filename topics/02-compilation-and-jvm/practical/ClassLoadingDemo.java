import java.sql.Connection;

/**
 * Shows class loaders, lazy loading and the order of static/instance initialisation.
 *
 * Run: java ClassLoadingDemo.java
 */
public class ClassLoadingDemo {

    static class Parent {
        static { System.out.println("  [1] Parent static block      (class initialisation, once)"); }
        { System.out.println("  [3] Parent instance block    (every new object)"); }
        Parent() { System.out.println("  [4] Parent constructor"); }
    }

    static class Child extends Parent {
        static { System.out.println("  [2] Child static block"); }
        { System.out.println("  [5] Child instance block"); }
        Child() { System.out.println("  [6] Child constructor"); }
    }

    static class Constants {
        static final int COMPILE_TIME = 42;                 // inlined: does NOT initialise the class
        static final Integer NOT_CONSTANT = 7;              // object: reading it DOES initialise the class
        static { System.out.println("  Constants initialised!"); }
    }

    static class Lazy {
        static { System.out.println("  Lazy initialised only when first used"); }
        static void touch() { }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== Class loaders ==");
        System.out.println("String           -> " + String.class.getClassLoader() + "  (null = bootstrap loader)");
        System.out.println("java.sql.Connection -> " + Connection.class.getClassLoader());
        System.out.println("ClassLoadingDemo -> " + ClassLoadingDemo.class.getClassLoader());

        System.out.println("\n== Initialisation order: first new Child() ==");
        new Child();
        System.out.println("== second new Child(): static blocks do not run again ==");
        new Child();

        System.out.println("\n== Constants ==");
        System.out.println("  COMPILE_TIME = " + Constants.COMPILE_TIME + "  (no init message above)");
        System.out.println("  NOT_CONSTANT = " + Constants.NOT_CONSTANT);

        System.out.println("\n== Lazy loading ==");
        System.out.println("  before touching Lazy");
        Lazy.touch();

        System.out.println("\n== Loading by name (reflection) ==");
        Class<?> c = Class.forName("java.util.ArrayList");
        System.out.println("  loaded " + c.getName() + " from module " + c.getModule().getName());
        try {
            Class.forName("com.example.DoesNotExist");
        } catch (ClassNotFoundException e) {
            System.out.println("  ClassNotFoundException: " + e.getMessage());
        }
    }
}
