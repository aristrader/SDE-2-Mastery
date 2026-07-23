package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.Vehicle;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;

@Data
@AllArgsConstructor
public class ParkingSpot {
  private String id;
  private VehicleType vehicleType;
  private Vehicle vehicle;

  public boolean isAvailable(){
    return this.vehicle == null;
  }

  public boolean canFit(VehicleType vehicleType){
    if (vehicleType == null){
      throw new RuntimeException("The vehicleType sent is null.");
    }
    return this.vehicleType == vehicleType;
  }

  public void reserve(Vehicle vehicle){
    if(vehicle == null){
      throw new RuntimeException("Vehicle is null.");
    }
    if(this.vehicle!=null){
      throw new RuntimeException("The spot is already full");
    }
    if(!this.vehicleType.equals(vehicle.getVehicleType())){
      throw new RuntimeException("Vehicle type is not compatible");
    }
    this.vehicle = vehicle;
  }

  public void release(){
    if(this.vehicle == null){
      throw new RuntimeException("The vehicle spot is already empty");
    }
    this.vehicle = null;
  }
}
