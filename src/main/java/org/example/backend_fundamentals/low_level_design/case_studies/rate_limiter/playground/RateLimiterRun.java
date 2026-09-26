package org.example.backend_fundamentals.low_level_design.case_studies.rate_limiter.playground;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Runnable checks for the agreed in-memory token-bucket behavior. */
public class RateLimiterRun {

  public static void main(String[] args) throws InterruptedException {
    verifiesCapacityAndRefill();
    verifiesIndependentKeys();
    verifiesConcurrentConsumption();
    verifiesInvalidInput();
    System.out.println("Rate Limiter checks passed.");
  }

  private static void verifiesCapacityAndRefill() throws InterruptedException {
    RateLimiter limiter = new RateLimiter(2, 10);
    RateLimitKey key = new RateLimitKey(1, "orders");

    require(limiter.tryConsume(key), "first request should pass");
    require(limiter.tryConsume(key), "second request should pass");
    require(!limiter.tryConsume(key), "request above capacity should fail");

    Thread.sleep(150);
    require(limiter.tryConsume(key), "refilled request should pass");
  }

  private static void verifiesIndependentKeys() {
    RateLimiter limiter = new RateLimiter(1, 0.000_000_001);

    require(limiter.tryConsume(new RateLimitKey(1, "orders")), "first key should pass");
    require(limiter.tryConsume(new RateLimitKey(2, "orders")), "second key should pass");
  }

  private static void verifiesConcurrentConsumption() throws InterruptedException {
    RateLimiter limiter = new RateLimiter(1, 0.000_000_001);
    RateLimitKey key = new RateLimitKey(1, "payments");
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch finished = new CountDownLatch(2);
    AtomicInteger allowedRequests = new AtomicInteger();

    Thread first = requestThread(limiter, key, start, finished, allowedRequests);
    Thread second = requestThread(limiter, key, start, finished, allowedRequests);
    first.start();
    second.start();
    start.countDown();
    finished.await();

    require(allowedRequests.get() == 1, "only one concurrent request should consume one token");
  }

  private static Thread requestThread(
      RateLimiter limiter,
      RateLimitKey key,
      CountDownLatch start,
      CountDownLatch finished,
      AtomicInteger allowedRequests) {
    return new Thread(() -> {
      try {
        start.await();
        if (limiter.tryConsume(key)) {
          allowedRequests.incrementAndGet();
        }
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new AssertionError("concurrent check was interrupted", exception);
      } finally {
        finished.countDown();
      }
    });
  }

  private static void verifiesInvalidInput() {
    expectIllegalArgument(() -> new RateLimiter(0, 1));
    expectIllegalArgument(() -> new RateLimiter(1, Double.NaN));
    expectIllegalArgument(() -> new RateLimiter(1, 1).tryConsume(null));
  }

  private static void expectIllegalArgument(Runnable operation) {
    try {
      operation.run();
      throw new AssertionError("expected IllegalArgumentException");
    } catch (IllegalArgumentException ignored) {
      // Expected validation behavior.
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
