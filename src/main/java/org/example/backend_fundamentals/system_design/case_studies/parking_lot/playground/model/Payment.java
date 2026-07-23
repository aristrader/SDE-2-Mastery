package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.PaymentMethod;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.PaymentStatus;

@Data
@AllArgsConstructor
public class Payment {
  private String id;
  private String ticketId;
  private Double amount;
  private PaymentMethod paymentMethod;
  private PaymentStatus paymentStatus;
  private Instant paidAt;
}
