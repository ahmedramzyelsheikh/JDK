import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Dynamic dispatch, covariant returns, overriding rules, casting, and overload-resolution corner cases.
 * Run: java Polymorphism.java
 */
public class Polymorphism {

    // ---------- overriding ----------
    static class Animal {
        String sound() { return "..."; }
        Animal reproduce() { return new Animal(); }
        void load() throws IOException { }
        protected String name() { return "animal"; }
    }

    static class Dog extends Animal {
        @Override String sound() { return "Woof"; }
        @Override Dog reproduce() { return new Dog(); }            // covariant return type
        @Override void load() throws FileNotFoundException { }     // narrower checked exception: OK
        @Override public String name() { return "dog"; }           // wider access: OK
        void fetch() { System.out.println("  Dog fetches the ball"); }
    }

    static class Cat extends Animal {
        @Override String sound() { return "Meow"; }
    }

    // ---------- overloading ----------
    static String m1(long x) { return "m1(long)"; }
    static String m1(Integer x) { return "m1(Integer)"; }

    static String m2(Object x) { return "m2(Object)"; }
    static String m2(int... x) { return "m2(int...)"; }

    static String m3(Object x) { return "m3(Object)"; }
    static String m3(String x) { return "m3(String)"; }

    static String m4(int x) { return "m4(int)"; }
    static String m4(String x) { return "m4(String)"; }

    static String greet(Animal a) { return "greet(Animal)"; }
    static String greet(Dog d) { return "greet(Dog)"; }

    public static void main(String[] args) {
        System.out.println("=== Dynamic dispatch: the OBJECT decides ===");
        List<Animal> animals = List.of(new Animal(), new Dog(), new Cat());
        for (Animal a : animals) {
            System.out.println("  " + a.getClass().getSimpleName() + " says " + a.sound());
        }
        Dog puppy = new Dog().reproduce();                         // no cast needed thanks to covariant return
        System.out.println("  covariant return gives a " + puppy.getClass().getSimpleName());

        System.out.println("\n=== Upcast / downcast ===");
        Animal a = new Dog();                                      // upcast
        // a.fetch();                                              // compile error: Animal has no fetch()
        if (a instanceof Dog d) {                                  // pattern matching
            d.fetch();
        }
        try {
            Cat c = (Cat) a;                                       // compiles, fails at run time
            System.out.println(c);
        } catch (ClassCastException e) {
            System.out.println("  (Cat) dog -> ClassCastException");
        }

        System.out.println("\n=== Overload resolution: the DECLARED type decides ===");
        System.out.println("  m1(5)    -> " + m1(5) + "   (widening beats boxing)");
        System.out.println("  m2(5)    -> " + m2(5) + "  (boxing beats varargs)");
        System.out.println("  m3(null) -> " + m3(null) + "  (most specific)");
        System.out.println("  m4(null) -> " + m4(null) + "  (int cannot be null)");
        System.out.println("  m4('a')  -> " + m4('a') + "     (char widens to int)");
        Object o = "text";
        System.out.println("  m3(o) where o is a String at runtime -> " + m3(o) + "  (declared type Object)");
        Animal dogAsAnimal = new Dog();
        System.out.println("  greet(dogAsAnimal) -> " + greet(dogAsAnimal) + "  (overloads are NOT dynamic)");
    }
}
