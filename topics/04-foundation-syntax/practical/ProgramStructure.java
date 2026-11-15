import java.util.Arrays;

/**
 * Anatomy of a Java program.
 * Run: java ProgramStructure.java first second
 */
public class ProgramStructure {

    static final String APP_NAME = "JDK course";     // constant: static final, UPPER_SNAKE_CASE
    static int counter;                              // static field: default value 0
    String instanceField;                            // instance field: default value null

    /** Entry point: public, static, void, String[] parameter. */
    public static void main(String[] args) {
        System.out.println("App: " + APP_NAME);
        System.out.println("Arguments received: " + Arrays.toString(args) + " (count " + args.length + ")");
        System.out.println("Static field default: " + counter);
        System.out.println("Instance field default: " + new ProgramStructure().instanceField);

        int local;                                   // local: NO default value
        // System.out.println(local);                // compile error: might not have been initialized
        local = 10;
        System.out.println("Local after assignment: " + local);

        {                                            // a block creates a new scope
            int insideBlock = 5;
            System.out.println("Inside block: " + (local + insideBlock));
        }
        // insideBlock is not visible here
    }
}
