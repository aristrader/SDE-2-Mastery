package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.exceptions;

import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .orElse("Invalid request");
    return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_FAILED", message));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException exception) {
    return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", exception.getMessage()));
  }

  @ExceptionHandler(NoValidSpotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoValidSpot(NoValidSpotFoundException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("NO_VALID_SPOT_FOUND", exception.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "Something went wrong"));
  }
}
