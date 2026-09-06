package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.enums.VehicleType;

@Getter
@Setter
@NoArgsConstructor
public class EnterRequest {
  @NotBlank
  private String registrationNumber;

  @NotNull
  private VehicleType vehicleType;

  @NotBlank
  private String entryGateId;
}
