package com.jdk.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ParallelSumTest {

    @Test
    void sumsInParallel() throws Exception {
        long n = 1_000_000;
        assertThat(ParallelSum.sum(n, 8)).isEqualTo(n * (n + 1) / 2);
        assertThat(ParallelSum.sum(10, 3)).isEqualTo(55);
    }

    @Test
    void atomicCounterDoesNotLoseUpdates() throws InterruptedException {
        assertThat(ParallelSum.countConcurrently(100, 1_000)).isEqualTo(100_000);
    }

    @Test
    void composesCompletableFutures() throws Exception {
        assertThat(ParallelSum.greetAsync(" java ").get()).isEqualTo("Hello, JAVA!");
    }
}
