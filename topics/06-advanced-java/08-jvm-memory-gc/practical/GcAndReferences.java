import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Allocation churn and GC statistics, unreachable cycles, weak/soft references, WeakHashMap and Cleaner.
 * Run: java GcAndReferences.java          (add -Xlog:gc or -XX:+UseZGC to compare collectors)
 */
public class GcAndReferences {

    static final Cleaner CLEANER = Cleaner.create();

    static class Node {
        Node other;
        final byte[] payload = new byte[1024];
    }

    /** A resource that registers a cleanup action instead of overriding finalize(). */
    static class NativeResource implements AutoCloseable {
        private final Cleaner.Cleanable cleanable;

        NativeResource(String name) {
            Runnable cleanup = () -> System.out.println("  Cleaner released " + name);   // must not capture `this`
            this.cleanable = CLEANER.register(this, cleanup);
        }

        @Override
        public void close() {
            cleanable.clean();             // explicit close runs it immediately (at most once)
        }
    }

    public static void main(String[] args) throws Exception {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        System.out.println("Collectors: " + ManagementFactory.getGarbageCollectorMXBeans().stream().map(GarbageCollectorMXBean::getName).toList());
        System.out.printf("Heap used at start: %,d KB%n", memory.getHeapMemoryUsage().getUsed() / 1024);

        System.out.println("\n=== Allocation churn: 2 GB of short-lived objects ===");
        long checksum = 0;
        for (int i = 0; i < 2_000_000; i++) {
            byte[] temp = new byte[1024];                       // dies immediately -> cheap young-gen GC
            checksum += temp.length;
        }
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("  %-28s collections=%4d total time=%5d ms%n", gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }
        System.out.println("  allocated " + checksum / (1024 * 1024) + " MB, heap stayed small because almost everything died young");

        System.out.println("\n=== Cycles are not a problem ===");
        Node a = new Node();
        Node b = new Node();
        a.other = b;
        b.other = a;                                            // cycle
        WeakReference<Node> watchA = new WeakReference<>(a);
        a = null;
        b = null;                                               // cycle now unreachable from any root
        System.gc();
        Thread.sleep(100);
        System.out.println("  cycle collected? " + (watchA.get() == null));

        System.out.println("\n=== Weak vs soft vs strong ===");
        Object strongTarget = new byte[1024];
        WeakReference<Object> weak = new WeakReference<>(new byte[1024]);
        SoftReference<Object> soft = new SoftReference<>(new byte[1024]);
        System.gc();
        Thread.sleep(100);
        System.out.println("  strong alive: " + (strongTarget != null)
                + ", weak cleared: " + (weak.get() == null)
                + ", soft kept (memory not low): " + (soft.get() != null));

        System.out.println("\n=== WeakHashMap: entries vanish when keys are unreachable ===");
        Map<Object, String> metadata = new WeakHashMap<>();
        Object session = new Object();
        metadata.put(session, "session data");
        metadata.put(new Object(), "orphan data");              // key unreachable right away
        System.gc();
        Thread.sleep(100);
        System.out.println("  size after GC: " + metadata.size() + " (only the entry whose key is still referenced)");

        System.out.println("\n=== Cleaner instead of finalize() ===");
        try (NativeResource r = new NativeResource("explicitly closed")) {
            System.out.println("  using resource");
        }
        new NativeResource("forgotten (cleaned by GC)");
        System.gc();
        Thread.sleep(200);

        System.out.printf("%nHeap used at end: %,d KB%n", memory.getHeapMemoryUsage().getUsed() / 1024);
        System.out.println("(System.gc() is only a hint; never call it in production code)");
    }
}
