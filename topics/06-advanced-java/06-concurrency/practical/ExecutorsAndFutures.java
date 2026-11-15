import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread pools (ThreadPoolExecutor, rejection), Callable/Future, invokeAll, scheduling,
 * CompletableFuture composition and error handling, CountDownLatch, Semaphore, BlockingQueue.
 * Run: java ExecutorsAndFutures.java
 */
public class ExecutorsAndFutures {

    record User(String name) { }
    record Orders(int count) { }

    public static void main(String[] args) throws Exception {
        System.out.println("=== ThreadPoolExecutor with a bounded queue ===");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2), new ThreadPoolExecutor.AbortPolicy());
        int rejected = 0;
        for (int i = 0; i < 6; i++) {
            try {
                pool.execute(() -> sleep(200));
            } catch (RejectedExecutionException e) {
                rejected++;
            }
        }
        System.out.println("  2 threads + queue of 2 = 4 accepted, rejected " + rejected + " (AbortPolicy)");
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("\n=== Callable, Future, invokeAll ===");
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {     // AutoCloseable since Java 19
            List<Callable<Integer>> jobs = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                int n = i;
                jobs.add(() -> { sleep(100); return n * n; });
            }
            long start = System.nanoTime();
            int sum = 0;
            for (Future<Integer> f : executor.invokeAll(jobs)) {
                sum += f.get();
            }
            System.out.printf("  sum of squares = %d in %d ms (4 x 100 ms jobs ran in parallel)%n", sum, (System.nanoTime() - start) / 1_000_000);
            Future<String> slow = executor.submit(() -> { sleep(1_000); return "late"; });
            try {
                slow.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("  Future.get(timeout) -> TimeoutException, cancel: " + slow.cancel(true));
            }
        }

        System.out.println("\n=== Scheduled executor ===");
        AtomicInteger ticks = new AtomicInteger();
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            scheduler.scheduleAtFixedRate(ticks::incrementAndGet, 0, 50, TimeUnit.MILLISECONDS);
            sleep(275);
            scheduler.shutdownNow();
        }
        System.out.println("  ticks every 50 ms for ~275 ms: " + ticks.get());

        System.out.println("\n=== CompletableFuture ===");
        try (ExecutorService io = Executors.newVirtualThreadPerTaskExecutor()) {
            long start = System.nanoTime();
            CompletableFuture<User> user = CompletableFuture.supplyAsync(() -> { sleep(200); return new User("Ali"); }, io);
            CompletableFuture<Orders> orders = CompletableFuture.supplyAsync(() -> { sleep(200); return new Orders(3); }, io);
            String dashboard = user.thenCombine(orders, (u, o) -> u.name() + " has " + o.count() + " orders")
                    .thenApply(String::toUpperCase)
                    .join();
            System.out.printf("  thenCombine: %s in %d ms (both calls in parallel)%n", dashboard, (System.nanoTime() - start) / 1_000_000);

            CompletableFuture<Integer> composed = CompletableFuture.supplyAsync(() -> 5, io)
                    .thenCompose(n -> CompletableFuture.supplyAsync(() -> n * 10, io));   // flatMap
            System.out.println("  thenCompose: " + composed.join());

            CompletableFuture<String> failing = CompletableFuture
                    .supplyAsync(() -> { throw new IllegalStateException("payment service down"); }, io)
                    .thenApply(Object::toString)                                        // skipped on failure
                    .exceptionally(ex -> "fallback (" + ex.getCause().getMessage() + ")");
            System.out.println("  exceptionally: " + failing.join());

            CompletableFuture<String> timeout = CompletableFuture.supplyAsync(() -> { sleep(1_000); return "slow"; }, io)
                    .completeOnTimeout("default after timeout", 100, TimeUnit.MILLISECONDS);
            System.out.println("  completeOnTimeout: " + timeout.join());

            List<CompletableFuture<Integer>> many = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                int n = i;
                many.add(CompletableFuture.supplyAsync(() -> n, io));
            }
            CompletableFuture.allOf(many.toArray(CompletableFuture[]::new)).join();
            System.out.println("  allOf sum: " + many.stream().mapToInt(CompletableFuture::join).sum());
        }

        System.out.println("\n=== CountDownLatch: wait for 3 services ===");
        CountDownLatch ready = new CountDownLatch(3);
        for (String service : List.of("db", "cache", "queue")) {
            Thread.ofVirtual().start(() -> {
                sleep(50);
                System.out.println("  " + service + " ready");
                ready.countDown();
            });
        }
        ready.await();
        System.out.println("  all services ready -> start accepting requests");

        System.out.println("\n=== Semaphore: at most 3 concurrent calls ===");
        Semaphore permits = new Semaphore(3);
        AtomicInteger current = new AtomicInteger();
        AtomicInteger maxSeen = new AtomicInteger();
        try (ExecutorService vexec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; i++) {
                vexec.submit(() -> {
                    permits.acquireUninterruptibly();
                    try {
                        maxSeen.accumulateAndGet(current.incrementAndGet(), Math::max);
                        sleep(30);
                    } finally {
                        current.decrementAndGet();
                        permits.release();
                    }
                });
            }
        }
        System.out.println("  20 tasks, max concurrent observed = " + maxSeen.get());

        System.out.println("\n=== Producer / consumer with BlockingQueue ===");
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        final int poison = -1;
        Thread producer = Thread.ofPlatform().start(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    queue.put(i);                  // blocks when full
                }
                queue.put(poison);                 // tell the consumer to stop
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        AtomicInteger consumed = new AtomicInteger();
        Thread consumer = Thread.ofPlatform().start(() -> {
            try {
                while (true) {
                    int item = queue.take();       // blocks when empty
                    if (item == poison) {
                        break;
                    }
                    consumed.addAndGet(item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producer.join();
        consumer.join();
        System.out.println("  consumed sum 1..10 = " + consumed.get());
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
