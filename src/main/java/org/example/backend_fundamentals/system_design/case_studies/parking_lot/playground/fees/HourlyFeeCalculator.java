package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.fees;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.Ticket;
import org.springframework.stereotype.Component;

@Component
public class HourlyFeeCalculator implements FeeCalculator {

  private static final Map<VehicleType, Double> VEHICLE_TYPE_PER_HOUR_COST;
  static {
    EnumMap<VehicleType, Double> rates = new EnumMap<>(VehicleType.class);
    rates.put(VehicleType.BIKE, 50.00);
    rates.put(VehicleType.CAR, 100.00);
    rates.put(VehicleType.TRUCK, 200.00);
    VEHICLE_TYPE_PER_HOUR_COST = Map.copyOf(rates);
  }


  @Override
  public Double collect(Ticket ticket, Instant exitTime) {
    if(ticket == null) {
      throw new IllegalArgumentException("No ticket available to calculate the pricing.");
    }
    if(exitTime == null || exitTime.isBefore(ticket.getEntryTime())) {
      throw new IllegalArgumentException("Invalid exit time supplied : " + exitTime);
    }
    long minutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();
    long hours = Math.max(1, (minutes + 59) / 60);
    return VEHICLE_TYPE_PER_HOUR_COST.get(ticket.getVehicle().getVehicleType())
        * hours;
  }
}
