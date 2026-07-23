package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums;

import lombok.Getter;

@Getter
public enum VehicleType {
  CAR("CAR", 1),
  BIKE("Bike", 2),
  TRUCK("TRUCK", 3);

  VehicleType(String name, int id){
    this.name = name;
    this.id = id;
  }

  private final String name;
  private final int id;
}
