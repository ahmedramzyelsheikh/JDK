import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A classic leak (a static list that only grows) vs a bounded cache. Watch "heap after GC":
 * with the leak it keeps rising until OutOfMemoryError; with the bounded cache it stays flat.
 * Run: java -Xmx64m MemoryLeakDemo.java
 *      java -Xmx64m -XX:+HeapDumpOnOutOfMemoryError MemoryLeakDemo.java   (then open the .hprof in Eclipse MAT)
 */
public class MemoryLeakDemo {

    record Request(long id, byte[] body) { }

    static final List<Request> AUDIT_LOG = new ArrayList<>();         // LEAK: never cleared

    static final Map<Long, Request> RECENT = new LinkedHashMap<>(16, 0.75f, true) {   // bounded LRU
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Request> eldest) {
            return size() > 100;
        }
    };

    public static void main(String[] args) {
        System.out.println("=== Bounded cache ===");
        simulate(false);
        System.out.println("\n=== Leaking static list ===");
        try {
            simulate(true);
        } catch (OutOfMemoryError e) {
            int size = AUDIT_LOG.size();
            AUDIT_LOG.clear();                                         // free memory so we can print
            System.out.println("  OutOfMemoryError: " + e.getMessage() + " after keeping " + size + " requests alive");
        }
    }

    static void simulate(boolean leak) {
        for (long id = 1; id <= 20_000; id++) {
            Request r = new Request(id, new byte[10 * 1024]);           // 10 KB per request
            if (leak) {
                AUDIT_LOG.add(r);
            } else {
                RECENT.put(id, r);
            }
            if (id % 2_000 == 0) {
                System.gc();
                long used = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024);
                System.out.printf("  after %,6d requests: heap after GC ~ %3d MB%n", id, used);
            }
        }
    }
}
