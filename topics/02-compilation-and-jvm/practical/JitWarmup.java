/**
 * Runs the same work several times. The first rounds run in the interpreter,
 * later rounds use C1/C2-compiled code and get much faster.
 *
 * Run:  java JitWarmup.java
 *       java -Xint JitWarmup.java                (interpreter only, compare!)
 *       java -XX:+PrintCompilation JitWarmup.java
 */
public class JitWarmup {

    static long work(int n) {
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (i % 7) * (i % 3) + Integer.bitCount(i);
        }
        return sum;
    }

    public static void main(String[] args) {
        long checksum = 0;
        for (int round = 1; round <= 10; round++) {
            long start = System.nanoTime();
            checksum += work(2_000_000);
            long micros = (System.nanoTime() - start) / 1_000;
            System.out.printf("round %2d: %,8d us%n", round, micros);
        }
        System.out.println("checksum " + checksum + " (printed so the JIT cannot remove the work)");
    }
}
