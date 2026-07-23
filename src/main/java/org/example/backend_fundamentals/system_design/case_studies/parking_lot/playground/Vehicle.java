package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;

@Data
@AllArgsConstructor
public class Vehicle {
  private String registrationNumber;
  private VehicleType vehicleType;
}
