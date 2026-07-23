package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;

@Getter
public class ParkingLot {
  private final String id;
  private final List<ParkingFloor> parkingFloors;

  public ParkingLot(String id, List<ParkingFloor> parkingFloors) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("parking lot id is required");
    }
    if (parkingFloors == null || parkingFloors.contains(null)) {
      throw new IllegalArgumentException("parking floors are required");
    }

    this.id = id;
    this.parkingFloors = List.copyOf(parkingFloors);
  }

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
    if (vehicleType == null) {
      throw new IllegalArgumentException("vehicle type is required");
    }

    return parkingFloors.stream()
        .map(parkingFloor -> parkingFloor.getAvailability(vehicleType))
        .reduce(0, Integer::sum);
  }
}
