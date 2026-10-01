package org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground;

import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground.model.Direction;
import org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground.model.DoorState;
import org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground.model.Elevator;
import org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground.service.ElevatorService;

/** Runnable checks for the agreed in-memory elevator scope. */
public class ElevatorRun {

  public static void main(String[] args) {
    verifiesNearestIdleAssignment();
    verifiesEligibleElevatorWinsOverFallback();
    verifiesDirectionalStopsAndReversal();
    verifiesLowerIdTieBreak();
    verifiesInvalidInput();
    System.out.println("Elevator checks passed.");
  }

  private static void verifiesNearestIdleAssignment() {
    Elevator first = new Elevator();
    Elevator second = new Elevator();
    first.setCurrentFloor(2);
    second.setCurrentFloor(7);

    ElevatorService service = new ElevatorService(10, List.of(first, second));
    require(service.assignElevator(4, Direction.UP) == first, "nearest idle elevator should be chosen");
  }

  private static void verifiesEligibleElevatorWinsOverFallback() {
    Elevator eligible = new Elevator();
    Elevator fallback = new Elevator();
    eligible.setCurrentFloor(4);
    eligible.setCurrentDirection(Direction.UP);
    fallback.setCurrentFloor(7);
    fallback.setCurrentDirection(Direction.DOWN);

    ElevatorService service = new ElevatorService(10, List.of(eligible, fallback));
    require(service.assignElevator(8, Direction.UP) == eligible,
        "eligible elevator should be chosen before fallback");
  }

  private static void verifiesDirectionalStopsAndReversal() {
    Elevator elevator = new Elevator();
    elevator.addFloorRequest(3);
    elevator.addHallRequest(1, Direction.DOWN);

    advance(elevator, 4);
    require(elevator.getCurrentFloor() == 3 && elevator.getDoorState() == DoorState.OPEN,
        "upward stop should be served before reversal");

    advance(elevator, 5);
    require(elevator.getCurrentFloor() == 1 && elevator.getDoorState() == DoorState.OPEN,
        "downward stop should be served after reversal");
  }

  private static void verifiesLowerIdTieBreak() {
    Elevator lowerId = new Elevator();
    Elevator higherId = new Elevator();
    lowerId.setCurrentFloor(4);
    higherId.setCurrentFloor(4);

    ElevatorService service = new ElevatorService(10, List.of(higherId, lowerId));
    require(service.assignElevator(6, Direction.UP) == lowerId,
        "equal candidates should choose lower elevator ID");
  }

  private static void verifiesInvalidInput() {
    expectIllegalArgument(() -> new ElevatorService(10, List.of(new Elevator())));

    Elevator first = new Elevator();
    Elevator second = new Elevator();
    ElevatorService service = new ElevatorService(10, List.of(first, second));
    expectIllegalArgument(() -> service.assignElevator(0, Direction.DOWN));
    expectIllegalArgument(() -> service.assignElevator(10, Direction.UP));
    expectIllegalArgument(() -> service.addFloorStop(-1, 1));
  }

  private static void advance(Elevator elevator, int steps) {
    for (int step = 0; step < steps; step++) {
      elevator.advanceOneStep();
    }
  }

  private static void expectIllegalArgument(Runnable operation) {
    try {
      operation.run();
      throw new AssertionError("expected IllegalArgumentException");
    } catch (IllegalArgumentException ignored) {
      // Expected validation behavior.
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
