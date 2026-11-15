import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 10,000 I/O-like tasks (each sleeps 100 ms): a fixed pool of 100 platform threads vs
 * one virtual thread per task. Also shows that virtual threads don't help CPU-bound work.
 * Run: java VirtualThreadsDemo.java
 */
public class VirtualThreadsDemo {

    static final int TASKS = 10_000;

    public static void main(String[] args) {
        System.out.println("Cores: " + Runtime.getRuntime().availableProcessors());

        long platform = time(Executors.newFixedThreadPool(100), VirtualThreadsDemo::ioTask);
        System.out.printf("I/O-bound, 100 platform threads:  %,6d ms  (10,000 tasks / 100 threads x 100 ms)%n", platform);

        long virtual = time(Executors.newVirtualThreadPerTaskExecutor(), VirtualThreadsDemo::ioTask);
        System.out.printf("I/O-bound, virtual thread per task: %,5d ms  (all 10,000 wait at the same time)%n", virtual);

        AtomicInteger carriers = new AtomicInteger();
        try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 5).forEach(i -> ex.submit(() -> {
                String t = Thread.currentThread().toString();     // VirtualThread[#id]/runnable@ForkJoinPool-1-worker-N
                if (carriers.incrementAndGet() <= 2) {
                    System.out.println("  " + t);
                }
            }));
        }

        long cpuPlatform = timeCpu(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        long cpuVirtual = timeCpu(Executors.newVirtualThreadPerTaskExecutor());
        System.out.printf("CPU-bound: fixed pool %d ms vs virtual %d ms -> same ballpark: CPU work is limited by cores, not threads%n",
                cpuPlatform, cpuVirtual);
    }

    static void ioTask() {
        try {
            Thread.sleep(Duration.ofMillis(100));                  // simulates waiting for a DB / HTTP call
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static long time(ExecutorService executor, Runnable task) {
        long start = System.nanoTime();
        try (executor) {
            for (int i = 0; i < TASKS; i++) {
                executor.submit(task);
            }
        }                                                          // close() waits for all tasks
        return (System.nanoTime() - start) / 1_000_000;
    }

    static long timeCpu(ExecutorService executor) {
        long start = System.nanoTime();
        try (executor) {
            for (int i = 0; i < 64; i++) {
                executor.submit(() -> {
                    long x = 0;
                    for (int k = 0; k < 20_000_000; k++) {
                        x += k % 7;
                    }
                    return x;
                });
            }
        }
        return (System.nanoTime() - start) / 1_000_000;
    }
}
