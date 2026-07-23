package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.PaymentMethod;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.enums.VehicleType;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.fees.FeeCalculator;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.fees.FeeCalculatorResolver;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.ParkingAssignment;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.ParkingLot;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.Ticket;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.Vehicle;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.repository.TicketRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParkingService {
  private final ParkingLot parkingLot;
  private final TicketRepository ticketRepository;
  private final FeeCalculatorResolver feeCalculatorResolver;
  private final PaymentService paymentService;

  public Ticket enterVehicle(Vehicle vehicle, String entryGateId) {
    ParkingAssignment parkingAssignment = parkingLot.allocateSpot(vehicle);
    Ticket ticket = new Ticket(UUID.randomUUID().toString(), vehicle,
        parkingAssignment.getParkingSpot().getId(), parkingAssignment.getFloorNumber(), entryGateId,
        Instant.now());
    ticketRepository.save(ticket);
    return ticket;
  }

  public Double getExitQuote(String ticketId) {
    Ticket ticket = ticketRepository.findById(ticketId);
    FeeCalculator feeCalculator = feeCalculatorResolver.resolve();
    return feeCalculator.collect(ticket, Instant.now());
  }

  public void completeExit(String ticketId, PaymentMethod paymentMethod, String exitGateId) {
    Ticket ticket = ticketRepository.findById(ticketId);
    FeeCalculator feeCalculator = feeCalculatorResolver.resolve();
    Instant exitTime = Instant.now();
    Double fee = feeCalculator.collect(ticket, exitTime);
    paymentService.collect(ticketId, fee, paymentMethod);
    parkingLot.releaseSpot(ticket.getSpotId());
    ticket.close(exitTime, exitGateId);
  }

  public int getAvailability(VehicleType vehicleType) {
    return parkingLot.getAvailability(vehicleType);
  }
}
