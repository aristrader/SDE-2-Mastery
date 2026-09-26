package org.example.backend_fundamentals.low_level_design.case_studies.rate_limiter.playground;

import lombok.Data;

@Data
public class RateLimitKey {
  private final int clientId;
  private final String key;

  public RateLimitKey(int clientId, String key) {
    if(key==null || key.isBlank()){
      throw new IllegalArgumentException("Blank keys are not allowed");
    }

    this.clientId = clientId;
    this.key = key;
  }
}
