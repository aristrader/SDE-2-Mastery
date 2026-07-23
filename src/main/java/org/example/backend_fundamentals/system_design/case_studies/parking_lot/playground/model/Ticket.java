package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.Vehicle;

@Data
@AllArgsConstructor
public class Ticket {
  private String id;
  private Vehicle vehicle;
  private String spotId;
  private int floorNumber;
  private String entryGateId;
  private String exitGateId;
  private Instant entryTime;
  private Instant exitTime;

  public boolean isActive() {
    return this.exitTime == null;
  }

  public void close(Instant exitTime, String exitGateId) {
    this.exitTime = exitTime;
    this.exitGateId = exitGateId;
  }
}
