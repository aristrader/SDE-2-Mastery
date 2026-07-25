package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.config;

import java.util.ArrayList;
import java.util.List;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.ParkingFloor;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.ParkingLot;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.ParkingSpot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParkingLotConfig {

  @Bean
  public ParkingLot parkingLot() {
    return new ParkingLot("LOT-1", new ArrayList<>(List.of(
        new ParkingFloor("FLOOR-1", 1, new ArrayList<>(List.of(
            new ParkingSpot("CAR-1", VehicleType.CAR),
            new ParkingSpot("BIKE-1", VehicleType.BIKE)
        )))
    )));
  }
}
