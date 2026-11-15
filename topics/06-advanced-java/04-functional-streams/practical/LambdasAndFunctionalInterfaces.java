import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Lambda syntax, the core functional interfaces, composition, method references,
 * custom functional interfaces and captured variables.
 * Run: java LambdasAndFunctionalInterfaces.java
 */
public class LambdasAndFunctionalInterfaces {

    @FunctionalInterface
    interface DiscountPolicy {
        double apply(double price);                       // exactly one abstract method

        default DiscountPolicy then(DiscountPolicy next) { // default methods are allowed
            return price -> next.apply(apply(price));
        }
    }

    record Product(String name, double price) { }

    public static void main(String[] args) {
        System.out.println("=== Core interfaces ===");
        Supplier<List<String>> listFactory = ArrayList::new;
        Consumer<String> printer = s -> System.out.println("  consumed: " + s);
        Function<String, Integer> length = String::length;
        BiFunction<Integer, Integer, Integer> power = (base, exp) -> (int) Math.pow(base, exp);
        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
        BinaryOperator<Integer> sum = Integer::sum;
        Predicate<String> isEmpty = String::isEmpty;
        IntPredicate isEven = n -> n % 2 == 0;            // primitive specialisation: no boxing

        List<String> list = listFactory.get();
        list.add("lambda");
        printer.accept(list.get(0));
        System.out.println("  length(\"lambda\")=" + length.apply("lambda") + " power(2,10)=" + power.apply(2, 10)
                + " shout(\"hi\")=" + shout.apply("hi") + " sum(3,4)=" + sum.apply(3, 4)
                + " isEmpty(\"\")=" + isEmpty.test("") + " isEven(7)=" + isEven.test(7));

        System.out.println("\n=== Composition ===");
        Predicate<String> notBlank = s -> !s.isBlank();
        Predicate<String> shortWord = s -> s.length() <= 5;
        Predicate<String> valid = notBlank.and(shortWord).or("admin"::equals);
        for (String s : new String[] {"java", "   ", "spring-boot", "admin"}) {
            System.out.printf("  valid(\"%s\") = %s, negate = %s%n", s, valid.test(s), valid.negate().test(s));
        }
        Function<Integer, Integer> plus1 = x -> x + 1;
        Function<Integer, Integer> times2 = x -> x * 2;
        System.out.println("  plus1.andThen(times2)(3) = " + plus1.andThen(times2).apply(3)
                + ", plus1.compose(times2)(3) = " + plus1.compose(times2).apply(3)
                + ", identity(5) = " + Function.<Integer>identity().apply(5));

        System.out.println("\n=== Custom functional interface ===");
        DiscountPolicy tenPercent = p -> p * 0.9;
        DiscountPolicy minus50 = p -> Math.max(0, p - 50);
        DiscountPolicy both = tenPercent.then(minus50);
        Product laptop = new Product("Laptop", 1000);
        System.out.println("  " + laptop.name() + ": 10% -> " + tenPercent.apply(laptop.price())
                + ", 10% then -50 -> " + both.apply(laptop.price()));

        System.out.println("\n=== Method references ===");
        Function<String, Integer> parse = Integer::parseInt;              // static
        Consumer<Object> out = System.out::println;                        // particular object
        Function<String, String> upper = String::toUpperCase;              // arbitrary object of a type
        Function<String, StringBuilder> ctor = StringBuilder::new;         // constructor
        java.util.function.IntFunction<String[]> arrayCtor = String[]::new; // array constructor
        System.out.print("  ");
        out.accept(parse.apply("41") + 1 + " " + upper.apply("ref") + " " + ctor.apply("abc").reverse() + " " + arrayCtor.apply(3).length);

        System.out.println("\n=== Captured variables ===");
        int bonus = 100;                                  // effectively final
        Function<Integer, Integer> addBonus = salary -> salary + bonus;
        System.out.println("  addBonus(5000) = " + addBonus.apply(5000));
        // bonus = 200;                                   // would break the lambda: must be effectively final
        int[] counter = {0};                              // work-around (mutable holder): avoid in real code
        List.of("a", "b", "c").forEach(x -> counter[0]++);
        System.out.println("  counted with array holder: " + counter[0]);
    }
}
