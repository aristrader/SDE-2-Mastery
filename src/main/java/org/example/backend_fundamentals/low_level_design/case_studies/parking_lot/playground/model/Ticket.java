package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model;

import java.time.Instant;
import lombok.Getter;

@Getter
public class Ticket {
  private final String id;
  private final Vehicle vehicle;
  private final String spotId;
  private final Integer floorNumber;
  private final String entryGateId;
  private final Instant entryTime;
  private String exitGateId;
  private Instant exitTime;

  public Ticket(String id, Vehicle vehicle, String spotId, Integer floorNumber, String entryGateId,
      Instant entryTime) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("ticket id is required");
    }
    if (vehicle == null) {
      throw new IllegalArgumentException("vehicle is required");
    }
    if (spotId == null || spotId.isBlank()) {
      throw new IllegalArgumentException("spot id is required");
    }
    if (floorNumber == null) {
      throw new IllegalArgumentException("floor number is required");
    }
    if (entryGateId == null || entryGateId.isBlank()) {
      throw new IllegalArgumentException("entry gate id is required");
    }
    if (entryTime == null) {
      throw new IllegalArgumentException("entry time is required");
    }

    this.id = id;
    this.vehicle = vehicle;
    this.spotId = spotId;
    this.floorNumber = floorNumber;
    this.entryGateId = entryGateId;
    this.entryTime = entryTime;
  }

  public boolean isActive() {
    return this.exitTime == null;
  }

  public void close(Instant exitTime, String exitGateId) {
    if(this.exitTime != null){
      throw new RuntimeException("Ticket already closed.");
    }
    if (exitTime == null) {
      throw new IllegalArgumentException("exit time is required");
    }
    if (exitGateId == null || exitGateId.isBlank()) {
      throw new IllegalArgumentException("exit gate id is required");
    }

    this.exitTime = exitTime;
    this.exitGateId = exitGateId;
  }
}
