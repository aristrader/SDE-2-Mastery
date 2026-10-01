package org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground.model.Direction;
import org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground.model.Elevator;

public class ElevatorService {
  private final int totalFloors;
  private final Map<Integer, Elevator> elevatorMap = new HashMap<>();

  public ElevatorService(int totalFloors, List<Elevator> elevators) {
    if(totalFloors <= 0 || elevators == null || elevators.size() < 2){
      throw new IllegalArgumentException("Total floors must be positive and at least two elevators are required.");
    }

    this.totalFloors = totalFloors;
    for(Elevator elevator : elevators){
      elevatorMap.put(elevator.getId(), elevator);
    }
  }

  public void advanceOneStep() {
    for(Elevator elevator : elevatorMap.values()){
      elevator.advanceOneStep();
    }
  }

  public void addFloorStop(int elevatorId, int floor) {
    if(floor<0 || floor>this.totalFloors){
      throw new IllegalArgumentException("Please call a floor between 0 and " + totalFloors);
    }

    Elevator elevator = elevatorMap.get(elevatorId);
    if (elevator == null) {
      throw new IllegalArgumentException("Unknown elevator ID: " + elevatorId);
    }
    elevator.addFloorRequest(floor);
  }

  public Elevator assignElevator(int requestFloor, Direction direction) {
    if(requestFloor<0 || requestFloor>this.totalFloors || direction == null || direction == Direction.STOP){
      throw new IllegalArgumentException("Please call a floor between 0 and " + totalFloors + " a non null direction");
    }
    if ((requestFloor == 0 && direction == Direction.DOWN)
        || (requestFloor == totalFloors && direction == Direction.UP)) {
      throw new IllegalArgumentException("Hall-call direction points outside the building.");
    }

    Elevator idleElevator = null;
    Elevator eligibleElevator = null;
    Elevator fallbackElevator = null;
    for(Elevator elevator : elevatorMap.values()){
      if(elevator.getCurrentDirection() == Direction.STOP) {
        idleElevator = chooseCloser(idleElevator, elevator, requestFloor);
      } else if(isEligibleElevator(elevator, direction, requestFloor)) {
        eligibleElevator = chooseCloser(eligibleElevator, elevator, requestFloor);
      } else {
        fallbackElevator = chooseCloser(fallbackElevator, elevator, requestFloor);
      }
    }

    Elevator assignedElevator = idleElevator != null
        ? idleElevator
        : eligibleElevator != null ? eligibleElevator : fallbackElevator;
    assignedElevator.addHallRequest(requestFloor, direction);
    return assignedElevator;
  }

  private Elevator chooseCloser(Elevator currentBest, Elevator candidate, int requestFloor) {
    if (currentBest == null) {
      return candidate;
    }

    int currentDistance = Math.abs(currentBest.getCurrentFloor() - requestFloor);
    int candidateDistance = Math.abs(candidate.getCurrentFloor() - requestFloor);
    if (candidateDistance < currentDistance
        || candidateDistance == currentDistance && candidate.getId() < currentBest.getId()) {
      return candidate;
    }
    return currentBest;
  }

  private boolean isEligibleElevator(Elevator elevator, Direction direction, int requestFloor) {
    return (elevator.getCurrentDirection() == Direction.UP && elevator.getCurrentDirection() == direction
        && requestFloor>elevator.getCurrentFloor())
        ||
        (elevator.getCurrentDirection() == Direction.DOWN && elevator.getCurrentDirection() == direction
            && requestFloor<elevator.getCurrentFloor());
  }
}
