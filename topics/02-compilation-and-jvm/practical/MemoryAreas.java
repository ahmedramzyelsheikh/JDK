import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Prints heap/metaspace usage, demonstrates StackOverflowError (stack) and,
 * with the "oom" argument, OutOfMemoryError (heap).
 *
 * Run: java MemoryAreas.java
 *      java -Xmx16m MemoryAreas.java oom
 */
public class MemoryAreas {

    static int depth = 0;

    static void recurse() {
        depth++;          // each call pushes a new frame on this thread's stack
        recurse();
    }

    public static void main(String[] args) {
        Runtime rt = Runtime.getRuntime();
        System.out.printf("CPUs: %d, max heap: %d MB, total heap: %d MB, free: %d MB%n",
                rt.availableProcessors(), rt.maxMemory() >> 20, rt.totalMemory() >> 20, rt.freeMemory() >> 20);

        ManagementFactory.getMemoryPoolMXBeans().forEach(pool ->
                System.out.printf("  pool %-35s type=%-8s used=%d KB%n",
                        pool.getName(), pool.getType(), pool.getUsage().getUsed() >> 10));

        System.out.println("GC in use: " + ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(b -> b.getName()).toList());

        try {
            recurse();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError after " + depth + " nested calls (stack is per thread)");
        }

        if (args.length > 0 && args[0].equals("oom")) {
            List<long[]> hog = new ArrayList<>();
            try {
                while (true) {
                    hog.add(new long[1_000_000]);   // 8 MB each, all reachable -> GC cannot free them
                }
            } catch (OutOfMemoryError e) {
                int blocks = hog.size();
                hog.clear();                         // release memory before printing
                System.out.println("OutOfMemoryError: " + e.getMessage() + " after " + blocks + " blocks of 8 MB");
            }
        }
    }
}
