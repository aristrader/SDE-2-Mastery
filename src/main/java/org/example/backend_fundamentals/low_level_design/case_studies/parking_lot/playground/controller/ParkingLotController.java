package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.enums.VehicleType;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.Ticket;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.Vehicle;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.request.EnterRequest;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.request.ExitRequest;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parking")
@RequiredArgsConstructor
@Slf4j
public class ParkingLotController {

  private final ParkingService parkingService;


  @GetMapping("/availability")
  public ResponseEntity<Integer> getVehicleAvailabilityByType(@RequestParam VehicleType vehicleType) {
    return ResponseEntity.ok(parkingService.getAvailability(vehicleType));
  }

  @GetMapping("/tickets/{ticketId}/quote")
  public ResponseEntity<Double> getExitQuote(@PathVariable String ticketId){
    return ResponseEntity.ok(parkingService.getExitQuote(ticketId));
  }

  @PostMapping("/tickets/{ticketId}/exit")
  public ResponseEntity<Void> exitVehicle(@PathVariable String ticketId,
      @Valid @RequestBody ExitRequest exitRequest){
    parkingService.completeExit(ticketId, exitRequest.getPaymentMethod(), exitRequest.getExitGateId());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/entry")
  public ResponseEntity<Ticket> enterVehicle(@Valid @RequestBody EnterRequest enterRequest){
    Vehicle vehicle = new Vehicle(enterRequest.getRegistrationNumber(), enterRequest.getVehicleType());
    return ResponseEntity.ok(parkingService.enterVehicle(vehicle, enterRequest.getEntryGateId()));
  }
}
