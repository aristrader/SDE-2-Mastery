package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.Vehicle;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;

@Data
@AllArgsConstructor
public class ParkingLot {
  private String id;
  private List<ParkingFloor> parkingFloors;

  public synchronized ParkingSpot allocateSpot(Vehicle vehicle) {
    if(vehicle == null){
      throw new RuntimeException("The vehicle object is null.");
    }
    for(ParkingFloor parkingFloor : parkingFloors) {
      if(parkingFloor.getAvailability(vehicle.getVehicleType())>0){
        Optional<ParkingSpot> parkingSpot = parkingFloor.reserveSpot(vehicle);
        if(parkingSpot.isPresent()){
          return parkingSpot.get();
        }
      }
    }
    throw new RuntimeException("No suitable parking spot found.");
  }

  public void releaseSpot(String spotId) {
    if(spotId == null){
      throw new RuntimeException("The spotId is null.");
    }
    for(ParkingFloor parkingFloor : parkingFloors) {
      boolean freed = parkingFloor.releaseSpot(spotId);
      if(freed){
        return;
      }
    }
    throw new RuntimeException("No spot found with the given spotId: " + spotId);
  }

  public int getAvailability(VehicleType vehicleType) {
    return parkingFloors.stream()
        .map(parkingFloor -> parkingFloor.getAvailability(vehicleType))
        .reduce(0, Integer::sum);
  }
}
