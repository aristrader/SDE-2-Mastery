package org.example.backend_fundamentals.low_level_design.case_studies.rate_limiter.playground;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Bucket {
  private double availableTokens;
  private long lastRefillNanos;
}
