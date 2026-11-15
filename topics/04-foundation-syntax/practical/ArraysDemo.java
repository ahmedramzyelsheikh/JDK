import java.util.Arrays;
import java.util.List;

/**
 * Creating arrays, default values, 2D/jagged arrays, java.util.Arrays helpers and corner cases.
 * Run: java ArraysDemo.java
 */
public class ArraysDemo {

    public static void main(String[] args) {
        int[] ints = new int[3];
        boolean[] flags = new boolean[2];
        String[] names = new String[2];
        double[] doubles = new double[2];
        System.out.println("defaults: " + Arrays.toString(ints) + " " + Arrays.toString(flags) + " "
                + Arrays.toString(names) + " " + Arrays.toString(doubles));

        int[] marks = {70, 95, 82, 60, 88};
        int min = marks[0], max = marks[0], sum = 0;
        for (int m : marks) {
            min = Math.min(min, m);
            max = Math.max(max, m);
            sum += m;
        }
        System.out.printf("marks %s -> min %d, max %d, avg %.1f%n", Arrays.toString(marks), min, max, (double) sum / marks.length);

        int[][] matrix = {{1, 2, 3}, {4, 5, 6}};
        int[][] jagged = {{1}, {2, 3}, {4, 5, 6}};
        System.out.println("matrix: " + Arrays.deepToString(matrix) + ", rows=" + matrix.length + ", cols=" + matrix[0].length);
        System.out.println("jagged: " + Arrays.deepToString(jagged) + ", row lengths: " + jagged[0].length + "," + jagged[1].length + "," + jagged[2].length);

        System.out.println("\n=== java.util.Arrays ===");
        int[] copy = Arrays.copyOf(marks, marks.length);
        Arrays.sort(copy);
        System.out.println("sorted copy: " + Arrays.toString(copy) + ", original untouched: " + Arrays.toString(marks));
        System.out.println("binarySearch(82) in sorted: index " + Arrays.binarySearch(copy, 82));
        int[] filled = new int[4];
        Arrays.fill(filled, 7);
        System.out.println("fill(7): " + Arrays.toString(filled));
        System.out.println("copyOfRange(1,3): " + Arrays.toString(Arrays.copyOfRange(marks, 1, 3)));
        System.out.println("stream sum: " + Arrays.stream(marks).sum());

        System.out.println("\n=== Corner cases ===");
        int[] a = {1, 2, 3};
        int[] b = a;                     // same array, two references
        b[0] = 99;
        System.out.println("b = a; b[0] = 99 -> a = " + Arrays.toString(a));
        int[] c = {99, 2, 3};
        System.out.println("a == c: " + (a == c) + ", a.equals(c): " + a.equals(c) + ", Arrays.equals(a, c): " + Arrays.equals(a, c));
        try {
            System.out.println(a[3]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("a[3] -> " + e.getMessage());
        }
        try {
            Object[] objects = new String[1];
            objects[0] = 1;              // compiles, fails at runtime
        } catch (ArrayStoreException e) {
            System.out.println("array covariance -> ArrayStoreException: " + e.getMessage());
        }
        List<String> fixed = Arrays.asList("x", "y");
        try {
            fixed.add("z");
        } catch (UnsupportedOperationException e) {
            System.out.println("Arrays.asList(...).add -> UnsupportedOperationException (fixed-size list)");
        }
        int[] empty = new int[0];
        System.out.println("empty array is allowed, length " + empty.length);
    }
}
