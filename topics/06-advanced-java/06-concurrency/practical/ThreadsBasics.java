import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Creating threads, start vs run, join, states, interruption, and daemon threads.
 * Run: java ThreadsBasics.java
 */
public class ThreadsBasics {

    static class MyThread extends Thread {                   // way 1: extend Thread (least flexible)
        MyThread() { super("extends-Thread"); }
        @Override public void run() { log("hello from a Thread subclass"); }
    }

    static void log(String msg) {
        System.out.printf("  [%-16s] %s%n", Thread.currentThread().getName(), msg);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Ways to create threads ===");
        Thread t1 = new MyThread();
        Thread t2 = new Thread(() -> log("hello from a Runnable lambda"), "runnable");   // way 2: Runnable
        Callable<Integer> job = () -> 6 * 7;                                              // way 3: Callable + FutureTask
        FutureTask<Integer> future = new FutureTask<>(job);
        Thread t3 = new Thread(future, "callable");
        Thread t4 = Thread.ofVirtual().name("virtual").unstarted(() -> log("hello from a virtual thread"));  // way 4
        for (Thread t : new Thread[] {t1, t2, t3, t4}) {
            t.start();
        }
        for (Thread t : new Thread[] {t1, t2, t3, t4}) {
            t.join();
        }
        System.out.println("  Callable result via FutureTask: " + future.get());

        System.out.println("\n=== start() vs run() ===");
        Thread wrong = new Thread(() -> log("run() executed in the CALLER thread"), "never-started");
        wrong.run();                                                     // just a method call
        Thread right = new Thread(() -> log("start() executed in a NEW thread"), "started");
        right.start();
        right.join();
        try {
            right.start();
        } catch (IllegalThreadStateException e) {
            System.out.println("  starting a thread twice -> IllegalThreadStateException");
        }

        System.out.println("\n=== States ===");
        Object lock = new Object();
        Thread sleeper = new Thread(() -> sleep(300), "sleeper");
        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "waiter");
        System.out.println("  before start: " + sleeper.getState());
        sleeper.start();
        waiter.start();
        sleep(100);
        System.out.println("  sleeping: " + sleeper.getState() + ", waiting: " + waiter.getState());
        synchronized (lock) {
            Thread blocked = new Thread(() -> { synchronized (lock) { } }, "blocked");
            blocked.start();
            sleep(100);
            System.out.println("  trying to enter a held lock: " + blocked.getState());
            lock.notifyAll();
        }
        sleeper.join();
        waiter.join();
        System.out.println("  after join: " + sleeper.getState());

        System.out.println("\n=== Cooperative interruption ===");
        Thread worker = new Thread(() -> {
            int loops = 0;
            while (!Thread.currentThread().isInterrupted()) {   // check the flag
                loops++;
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    log("interrupted while sleeping after " + loops + " loops, cleaning up");
                    Thread.currentThread().interrupt();          // restore flag to exit the loop
                }
            }
        }, "worker");
        worker.start();
        sleep(120);
        worker.interrupt();
        worker.join();

        System.out.println("\n=== Daemon threads ===");
        Thread daemon = new Thread(() -> {
            while (true) {
                sleep(1_000);                                    // would run forever
            }
        }, "daemon");
        daemon.setDaemon(true);
        daemon.start();
        System.out.println("  daemon? " + daemon.isDaemon() + " -> the JVM exits without waiting for it");
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
