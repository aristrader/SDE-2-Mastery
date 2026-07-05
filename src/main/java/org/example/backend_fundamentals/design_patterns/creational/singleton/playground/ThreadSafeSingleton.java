package org.example.backend_fundamentals.design_patterns.creational.singleton.playground;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Thread-safe singleton variants using {@code synchronized}.
 *
 * <p>{@link #getInstance()} synchronizes the full method — correct but holds the monitor on
 * every call. {@link #getInstanceUsingDoubleLocking()} skips the lock on the fast path but
 * requires the backing field to be {@code volatile}. For production, prefer
 * {@link BillPughSingleton}.
 */
public class ThreadSafeSingleton {

  private static ThreadSafeSingleton obj;
  private static ThreadSafeSingleton instance;

  private ThreadSafeSingleton() {}

  /** @return the singleton instance; synchronizes the whole method on every call */
  public static synchronized ThreadSafeSingleton getInstance() {
    if (obj == null) {
      obj = new ThreadSafeSingleton();
    }
    return obj;
  }

  /**
   * Double-checked locking variant — skips synchronization on the fast path.
   *
   * <p>The outer {@code null} check avoids acquiring the monitor once the instance exists.
   * The inner {@code null} check (inside {@code synchronized}) guards against two threads both
   * passing the outer check before either has created the instance.
   *
   * <p>The backing field must be {@code volatile}: without it the JMM allows instruction
   * reordering so another thread may see a non-null but partially constructed instance.
   */
  public static ThreadSafeSingleton getInstanceUsingDoubleLocking() {
    if (instance == null) {
      synchronized (ThreadSafeSingleton.class) {
        if (instance == null) {
          instance = new ThreadSafeSingleton();
        }
      }
    }
    return instance;
  }

  /**
   * Races 50 threads against both {@code getInstance} variants and reports how many distinct
   * instances each produced. A correct singleton must always report exactly one.
   *
   * @throws InterruptedException if the main thread is interrupted while waiting for workers
   */
  public static void main(String[] args) throws InterruptedException {
    int threadCount = 50;

    runConcurrencyTest("getInstance()", threadCount, ThreadSafeSingleton::getInstance);
    runConcurrencyTest(
        "getInstanceUsingDoubleLocking()",
        threadCount,
        ThreadSafeSingleton::getInstanceUsingDoubleLocking);
  }

  private static void runConcurrencyTest(
      String label, int threadCount, Supplier<ThreadSafeSingleton> supplier)
      throws InterruptedException {

    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    // Starting gate: all threads park here until countDown(), maximising the race on first-init.
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch doneGate = new CountDownLatch(threadCount);
    Set<Integer> identityHashes = ConcurrentHashMap.newKeySet();

    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              startGate.await();
              ThreadSafeSingleton result = supplier.get();
              // identityHashCode instead of equals/hashCode so a broken equals() can't hide a bug.
              identityHashes.add(System.identityHashCode(result));
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              doneGate.countDown();
            }
          });
    }

    startGate.countDown();
    doneGate.await();
    executor.shutdown();

    String verdict = identityHashes.size() == 1 ? "PASS" : "FAIL";
    System.out.printf(
        "%-35s -> %d threads saw %d unique instance(s). %s%n",
        label, threadCount, identityHashes.size(), verdict);
  }
}
