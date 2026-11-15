import java.util.Scanner;

/**
 * Reading input with Scanner, including the nextInt()/nextLine() trap.
 * Run interactively:  java InputOutput.java
 * Or pipe input:      "Ali`n25`nCairo" | java InputOutput.java      (PowerShell)
 *                     printf 'Ali\n25\nCairo\n' | java InputOutput.java   (bash)
 */
public class InputOutput {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.print("Name: ");
        String name = in.hasNextLine() ? in.nextLine() : "Guest";

        System.out.print("Age: ");
        int age = in.hasNextInt() ? in.nextInt() : 0;
        if (in.hasNextLine()) {
            in.nextLine();          // consume the rest of the line, otherwise the next nextLine() returns ""
        }

        System.out.print("City: ");
        String city = in.hasNextLine() ? in.nextLine() : "Unknown";

        System.out.println();
        System.out.printf("Hello %s, age %d, from %s. Next year you will be %d.%n", name, age, city, age + 1);
        System.err.println("(this line goes to the error stream)");
    }
}
