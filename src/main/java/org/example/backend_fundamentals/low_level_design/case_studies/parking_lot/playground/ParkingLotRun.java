package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground;

import java.util.ArrayList;
import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.enums.PaymentMethod;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.enums.VehicleType;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.exceptions.NoValidSpotFoundException;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.fees.FeeCalculatorResolver;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.fees.HourlyFeeCalculator;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.ParkingFloor;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.ParkingLot;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.ParkingSpot;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.Ticket;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.Vehicle;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.repository.TicketRepository;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.service.ParkingService;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.service.PaymentService;

public class ParkingLotRun {

  public static void main(String[] args) {
    ParkingLot parkingLot = new ParkingLot("LOT-1", new ArrayList<>(List.of(
        new ParkingFloor("FLOOR-1", 1, new ArrayList<>(List.of(
            new ParkingSpot("CAR-1", VehicleType.CAR),
            new ParkingSpot("BIKE-1", VehicleType.BIKE)
        )))
    )));

    ParkingService parkingService = new ParkingService(
        parkingLot,
        new TicketRepository(),
        new FeeCalculatorResolver(new HourlyFeeCalculator()),
        new PaymentService()
    );

    Ticket ticket = parkingService.enterVehicle(new Vehicle("KA-01-1234", VehicleType.CAR),
        "ENTRY-1");
    System.out.println("Allocated spot: " + ticket.getSpotId());
    System.out.println("Allocated floor: " + ticket.getFloorNumber());

    try {
      parkingService.enterVehicle(new Vehicle("KA-02-9999", VehicleType.CAR), "ENTRY-1");
    } catch (NoValidSpotFoundException exception) {
      System.out.println("Second car rejected: " + exception.getMessage());
    }

    System.out.println("Exit quote: " + parkingService.getExitQuote(ticket.getId()));
    parkingService.completeExit(ticket.getId(), PaymentMethod.UPI, "EXIT-1");
    System.out.println("Available car spots after exit: "
        + parkingService.getAvailability(VehicleType.CAR));
  }
}
