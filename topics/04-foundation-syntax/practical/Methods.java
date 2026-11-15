/**
 * Overloading, varargs, recursion and pass-by-value.
 * Run: java Methods.java
 */
public class Methods {

    static class Person {
        String name;

        Person(String name) {
            this.name = name;
        }
    }

    // ---- overloading: same name, different parameter lists ----
    static double area(double radius) {
        return Math.PI * radius * radius;
    }

    static double area(double width, double height) {
        return width * height;
    }

    static double area(int side) {
        return side * side;
    }

    // ---- varargs ----
    static int sum(int... numbers) {          // numbers is an int[]
        int total = 0;
        for (int n : numbers) {
            total += n;
        }
        return total;
    }

    // ---- recursion ----
    static long factorial(int n) {
        if (n <= 1) {
            return 1;                         // base case
        }
        return n * factorial(n - 1);          // recursive case
    }

    static long fibonacciIterative(int n) {
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return a;
    }

    // ---- pass-by-value ----
    static void change(int number, Person person) {
        number = 100;                         // changes the local copy only
        person.name = "Changed";              // changes the shared object
        person = new Person("Brand new");     // re-points the local copy only
    }

    static void swap(int a, int b) {          // cannot swap the caller's variables
        int tmp = a;
        a = b;
        b = tmp;
    }

    public static void main(String[] args) {
        System.out.printf("area(2.0)=%.2f area(2.0,3.0)=%.1f area(4)=%.1f%n", area(2.0), area(2.0, 3.0), area(4));
        System.out.println("sum()=" + sum() + " sum(1,2,3)=" + sum(1, 2, 3) + " sum(array)=" + sum(new int[] {4, 5}));
        System.out.println("factorial(10)=" + factorial(10) + " fibonacci(50)=" + fibonacciIterative(50));

        int x = 5;
        Person p = new Person("Ali");
        change(x, p);
        System.out.println("after change: x=" + x + " (unchanged), p.name=" + p.name + " (object modified)");

        int a = 1, b = 2;
        swap(a, b);
        System.out.println("after swap(a, b): a=" + a + ", b=" + b + "  -> Java is pass-by-value");

        try {
            factorial(-1 + Integer.MAX_VALUE / 1000);   // very deep recursion
        } catch (StackOverflowError e) {
            System.out.println("deep recursion -> StackOverflowError");
        }
    }
}
