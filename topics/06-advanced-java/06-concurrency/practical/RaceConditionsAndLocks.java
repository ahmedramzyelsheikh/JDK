import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lost updates, four fixes (synchronized, ReentrantLock, AtomicInteger, LongAdder), check-then-act
 * on maps, visibility without volatile, and a real deadlock detected with ThreadMXBean.
 * Run: java RaceConditionsAndLocks.java
 */
public class RaceConditionsAndLocks {

    static final int THREADS = 8;
    static final int INCREMENTS = 100_000;

    static int unsafeCount;
    static int syncCount;
    static final Object LOCK = new Object();
    static int lockCount;
    static final ReentrantLock REENTRANT = new ReentrantLock();
    static final AtomicInteger ATOMIC = new AtomicInteger();
    static final LongAdder ADDER = new LongAdder();

    static boolean plainFlag = true;              // NOT volatile
    static volatile boolean volatileFlag = true;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Lost updates and fixes (expected " + THREADS * INCREMENTS + ") ===");
        run("unsafe count++", () -> unsafeCount++);
        run("synchronized", () -> { synchronized (LOCK) { syncCount++; } });
        run("ReentrantLock", () -> {
            REENTRANT.lock();
            try {
                lockCount++;
            } finally {
                REENTRANT.unlock();                 // always in finally
            }
        });
        run("AtomicInteger", ATOMIC::incrementAndGet);
        run("LongAdder", ADDER::increment);
        System.out.printf("  %-15s %,d  <- lost updates%n", "unsafe", unsafeCount);
        System.out.printf("  %-15s %,d%n  %-15s %,d%n  %-15s %,d%n  %-15s %,d%n",
                "synchronized", syncCount, "ReentrantLock", lockCount, "AtomicInteger", ATOMIC.get(), "LongAdder", ADDER.sum());

        System.out.println("\n=== Compare-and-swap ===");
        AtomicInteger stock = new AtomicInteger(10);
        boolean sold = stock.compareAndSet(10, 9);
        boolean staleWrite = stock.compareAndSet(10, 8);      // fails: value is no longer 10
        System.out.println("  CAS 10->9: " + sold + ", CAS 10->8 with stale expectation: " + staleWrite + ", stock=" + stock.get());

        System.out.println("\n=== Check-then-act on a map ===");
        ConcurrentHashMap<String, Integer> visits = new ConcurrentHashMap<>();
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            threads.add(Thread.ofPlatform().start(() -> {
                for (int i = 0; i < 10_000; i++) {
                    visits.merge("home", 1, Integer::sum);       // atomic read-modify-write
                }
            }));
        }
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("  ConcurrentHashMap.merge count: " + visits.get("home") + " (expected " + THREADS * 10_000 + ")");

        System.out.println("\n=== Visibility ===");
        System.out.println("  plain flag:    reader stopped? " + readerStops(false) + "  (may be false: the JIT can hoist the read)");
        System.out.println("  volatile flag: reader stopped? " + readerStops(true));

        System.out.println("\n=== Deadlock ===");
        Object a = new Object();
        Object b = new Object();
        Thread t1 = daemon("t1", () -> { synchronized (a) { pause(); synchronized (b) { } } });
        Thread t2 = daemon("t2", () -> { synchronized (b) { pause(); synchronized (a) { } } });
        t1.start();
        t2.start();
        Thread.sleep(300);
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] deadlocked = mx.findDeadlockedThreads();
        System.out.println("  deadlocked threads found: " + (deadlocked == null ? 0 : deadlocked.length)
                + ", states " + t1.getState() + "/" + t2.getState());

        System.out.println("  fix: tryLock with timeout");
        ReentrantLock la = new ReentrantLock();
        ReentrantLock lb = new ReentrantLock();
        la.lock();                                              // simulate another thread holding A
        Thread polite = new Thread(() -> {
            try {
                if (la.tryLock(200, TimeUnit.MILLISECONDS)) {
                    la.unlock();
                } else {
                    System.out.println("  could not get lock A within 200 ms -> back off and retry later (no deadlock)");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        polite.start();
        polite.join();
        la.unlock();
        lb.lock();
        lb.unlock();
    }

    static void run(String label, Runnable increment) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            threads.add(Thread.ofPlatform().name(label + "-" + t).start(() -> {
                for (int i = 0; i < INCREMENTS; i++) {
                    increment.run();
                }
            }));
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    static boolean readerStops(boolean useVolatile) throws InterruptedException {
        plainFlag = true;
        volatileFlag = true;
        Thread reader = daemon("reader", () -> {
            if (useVolatile) {
                while (volatileFlag) { }
            } else {
                while (plainFlag) { }
            }
        });
        reader.start();
        Thread.sleep(200);                    // let the JIT compile the loop
        plainFlag = false;
        volatileFlag = false;
        reader.join(500);
        return !reader.isAlive();
    }

    static Thread daemon(String name, Runnable r) {
        Thread t = new Thread(r, name);
        t.setDaemon(true);                    // so the JVM can exit even if it never finishes
        return t;
    }

    static void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
