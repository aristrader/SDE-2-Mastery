package org.example.backend_fundamentals.low_level_design.case_studies.rate_limiter.playground;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
  private final ConcurrentMap<RateLimitKey, Bucket> bucketsByKey = new ConcurrentHashMap<>();
  private final double refillTokensPerSecond;
  private final int maxTokens;

  public RateLimiter(int maxTokens, double refillTokensPerSecond){
    if(maxTokens<=0 || !Double.isFinite(refillTokensPerSecond) || refillTokensPerSecond<=0){
      throw new IllegalArgumentException("MaxTokens and refillTokensPerSecond must be positive finite numbers");
    }
    this.maxTokens = maxTokens;
    this.refillTokensPerSecond = refillTokensPerSecond;
  }

  public boolean tryConsume(RateLimitKey rateLimitKey) {

    if(rateLimitKey == null){
      throw new IllegalArgumentException("Please pass non null rateLimitKey");
    }

    Bucket bucket = bucketsByKey.computeIfAbsent(rateLimitKey, r -> new Bucket(this.maxTokens, System.nanoTime()));

    synchronized (bucket) {
      refill(bucket);

      if(bucket.getAvailableTokens()>=1){
        bucket.setAvailableTokens(bucket.getAvailableTokens()-1);
        return true;
      }

      return false;
    }
  }

  private void refill(Bucket bucket) {
    long currentTime = System.nanoTime();
    long elapsedTime = currentTime - bucket.getLastRefillNanos();
    double refilledTokens = (elapsedTime)/1_000_000_000.0*this.refillTokensPerSecond;
    bucket.setAvailableTokens(
        Math.min(this.maxTokens, bucket.getAvailableTokens() + refilledTokens)
    );
    bucket.setLastRefillNanos(currentTime);
  }
}
