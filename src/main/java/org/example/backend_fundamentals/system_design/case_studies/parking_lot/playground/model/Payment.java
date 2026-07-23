package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import java.time.Instant;
import lombok.Getter;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.PaymentMethod;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.PaymentStatus;

@Getter
public class Payment {
  private final String id;
  private final String ticketId;
  private final Double amount;
  private final PaymentMethod paymentMethod;
  private final PaymentStatus paymentStatus;
  private final Instant paidAt;

  public Payment(String id, String ticketId, Double amount, PaymentMethod paymentMethod,
      PaymentStatus paymentStatus, Instant paidAt) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("payment id is required");
    }
    if (ticketId == null || ticketId.isBlank()) {
      throw new IllegalArgumentException("ticket id is required");
    }
    if (amount == null || amount <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    if (paymentMethod == null) {
      throw new IllegalArgumentException("payment method is required");
    }
    if (paymentStatus == null) {
      throw new IllegalArgumentException("payment status is required");
    }

    this.id = id;
    this.ticketId = ticketId;
    this.amount = amount;
    this.paymentMethod = paymentMethod;
    this.paymentStatus = paymentStatus;
    this.paidAt = paidAt;
  }
}
