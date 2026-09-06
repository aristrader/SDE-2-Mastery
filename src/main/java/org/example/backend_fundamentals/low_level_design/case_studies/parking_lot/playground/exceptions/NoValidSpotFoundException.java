package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.exceptions;

public class NoValidSpotFoundException extends RuntimeException {

  public NoValidSpotFoundException(String message) {
    super(message);
  }
}
