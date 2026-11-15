/**
 * Compile and inspect by hand:
 *   javac -d out Hello.java
 *   java -cp out Hello
 *   javap -c -v -cp out Hello
 */
public class Hello {

    static final int TIMES = 2;           // compile-time constant: inlined by javac

    interface Greeter {                   // nested type -> compiled to Hello$Greeter.class
        String greet(String name);
    }

    static int add(int a, int b) {        // javap shows: iload_0, iload_1, iadd, ireturn
        return a + b;
    }

    public static void main(String[] args) {
        Greeter greeter = name -> "Hello, " + name;   // invokedynamic (LambdaMetafactory)
        for (int i = 0; i < TIMES; i++) {
            System.out.println(greeter.greet("JVM #" + add(i, 1)));  // invokedynamic (string concat)
        }
    }
}
