package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.Vehicle;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;

@Data
@AllArgsConstructor
public class ParkingFloor {
  private String id;
  private Integer floorNumber;
  private List<ParkingSpot> parkingSpots;

  public synchronized Optional<ParkingSpot> reserveSpot(Vehicle vehicle) {
    if(vehicle == null){
      throw new RuntimeException("The vehicle object is null.");
    }
    Optional<ParkingSpot> spot =  parkingSpots.stream()
        .filter(parkingSpot -> parkingSpot.isAvailable() && parkingSpot.canFit(vehicle.getVehicleType()))
        .findFirst();
    spot.ifPresent(parkingSpot -> parkingSpot.reserve(vehicle));
    return spot;
  }

  public boolean releaseSpot(String spotId) {
    if(spotId == null){
      throw new RuntimeException("The spotId is null.");
    }
    Optional<ParkingSpot> parkingSpot = parkingSpots.stream()
        .filter(spot -> spot.getId().equals(spotId))
        .findFirst();
    if(parkingSpot.isPresent()){
      parkingSpot.get().release();
      return true;
    } else {
      return false;
    }
  }

  public int getAvailability(VehicleType vehicleType) {
    if(vehicleType == null){
      throw  new RuntimeException("The vehicleType is null.");
    }
    return (int) parkingSpots.stream()
        .filter(parkingSpot -> parkingSpot.getVehicleType().equals(vehicleType) && parkingSpot.isAvailable())
        .count();
  }
}
