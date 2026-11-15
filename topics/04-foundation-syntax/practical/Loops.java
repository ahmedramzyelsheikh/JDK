import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * for, while, do-while, for-each, break/continue, labels, and the remove-while-iterating trap.
 * Run: java Loops.java
 */
public class Loops {

    public static void main(String[] args) {
        System.out.print("for:        ");
        for (int i = 1; i <= 5; i++) {
            System.out.print(i + " ");
        }

        System.out.print("\nwhile:      ");
        int n = 1234, sum = 0;
        while (n > 0) {                      // unknown number of iterations
            sum += n % 10;
            n /= 10;
        }
        System.out.print("digit sum of 1234 = " + sum);

        System.out.print("\ndo-while:   ");
        int attempts = 0;
        do {
            attempts++;                      // runs at least once even if the condition is false
        } while (attempts < 0);
        System.out.print("ran " + attempts + " time(s) although the condition was false");

        System.out.print("\nfor-each:   ");
        for (String lang : new String[] {"C", "C++", "Java", "C#"}) {
            System.out.print(lang + " ");
        }

        System.out.print("\ncontinue:   ");
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) {
                continue;                    // skip even numbers
            }
            System.out.print(i + " ");
        }

        System.out.print("\nbreak:      ");
        for (int i = 1; ; i++) {             // infinite loop stopped by break
            if (i * i > 50) {
                System.out.print("first square > 50 is " + i + "^2");
                break;
            }
        }

        System.out.println("\nlabeled break: search a 2D array");
        int[][] grid = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        outer:
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == 5) {
                    System.out.println("  found 5 at row " + r + ", col " + c);
                    break outer;             // exits BOTH loops
                }
            }
        }

        System.out.println("pattern:");
        for (int row = 1; row <= 4; row++) {
            System.out.println("  " + " ".repeat(4 - row) + "*".repeat(2 * row - 1));
        }

        System.out.println("removing while iterating:");
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        try {
            for (Integer x : numbers) {
                if (x % 2 == 0) {
                    numbers.remove(x);
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("  for-each + remove -> ConcurrentModificationException");
        }
        numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        for (Iterator<Integer> it = numbers.iterator(); it.hasNext();) {
            if (it.next() % 2 == 0) {
                it.remove();                 // safe
            }
        }
        System.out.println("  Iterator.remove -> " + numbers);
        List<Integer> other = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        other.removeIf(x -> x % 2 == 0);     // simplest
        System.out.println("  removeIf        -> " + other);
    }
}
