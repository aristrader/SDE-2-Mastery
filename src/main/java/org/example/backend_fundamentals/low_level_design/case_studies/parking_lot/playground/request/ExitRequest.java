package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.enums.PaymentMethod;

@Getter
@Setter
@NoArgsConstructor
public class ExitRequest {
  @NotNull
  private PaymentMethod paymentMethod;

  @NotBlank
  private String exitGateId;
}
