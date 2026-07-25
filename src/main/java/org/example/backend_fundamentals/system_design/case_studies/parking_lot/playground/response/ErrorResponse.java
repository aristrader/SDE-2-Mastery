package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.response;

import lombok.Getter;

@Getter
public class ErrorResponse {
  private final String code;
  private final String message;

  public ErrorResponse(String code, String message) {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("error code is required");
    }
    if (message == null || message.isBlank()) {
      throw new IllegalArgumentException("error message is required");
    }

    this.code = code;
    this.message = message;
  }
}
