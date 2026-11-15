package com.jdk.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

/**
 * Topic 08 - Concurrency: ExecutorService, Future, CompletableFuture, atomics, virtual threads (Java 21).
 */
public final class ParallelSum {

    private ParallelSum() {
    }

    /** Split 1..n into chunks and sum each chunk on a virtual thread. */
    public static long sum(long n, int chunks) throws InterruptedException, ExecutionException {
        long chunkSize = Math.max(1, n / chunks);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Long>> futures = new ArrayList<>();
            for (long start = 1; start <= n; start += chunkSize) {
                long from = start;
                long to = Math.min(n, start + chunkSize - 1);
                futures.add(executor.submit(() -> LongStream.rangeClosed(from, to).sum()));
            }
            long total = 0;
            for (Future<Long> future : futures) {
                total += future.get();
            }
            return total;
        }
    }

    /** AtomicLong: a counter many threads can increment safely without locks. */
    public static long countConcurrently(int threads, int incrementsPerThread) throws InterruptedException {
        AtomicLong counter = new AtomicLong();
        List<Thread> started = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            started.add(Thread.ofVirtual().start(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.incrementAndGet();
                }
            }));
        }
        for (Thread thread : started) {
            thread.join();
        }
        return counter.get();
    }

    /** CompletableFuture: compose async steps without blocking in between. */
    public static CompletableFuture<String> greetAsync(String name) {
        return CompletableFuture.supplyAsync(() -> name.trim())
                .thenApply(String::toUpperCase)
                .thenApply(n -> "Hello, " + n + "!");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Sum 1..1,000,000 = " + sum(1_000_000, 8));
        System.out.println("Counter = " + countConcurrently(100, 1_000));
        System.out.println(greetAsync(" java ").get());
    }
}
