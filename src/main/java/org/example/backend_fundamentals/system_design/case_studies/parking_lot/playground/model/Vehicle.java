package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import lombok.Getter;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;

@Getter
public class Vehicle {
  private final String registrationNumber;
  private final VehicleType vehicleType;

  public Vehicle(String registrationNumber, VehicleType vehicleType) {
    if (registrationNumber == null || registrationNumber.isBlank()) {
      throw new IllegalArgumentException("registration number is required");
    }
    if (vehicleType == null) {
      throw new IllegalArgumentException("vehicle type is required");
    }

    this.registrationNumber = registrationNumber;
    this.vehicleType = vehicleType;
  }
}
