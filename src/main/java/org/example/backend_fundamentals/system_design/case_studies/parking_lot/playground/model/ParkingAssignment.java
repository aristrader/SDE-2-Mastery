package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import lombok.Getter;

@Getter
public class ParkingAssignment {
  private final ParkingSpot parkingSpot;
  private final Integer floorNumber;

  public ParkingAssignment(ParkingSpot parkingSpot, Integer floorNumber) {
    if (parkingSpot == null) {
      throw new IllegalArgumentException("parking spot is required");
    }
    if (floorNumber == null) {
      throw new IllegalArgumentException("floor number is required");
    }

    this.parkingSpot = parkingSpot;
    this.floorNumber = floorNumber;
  }
}
