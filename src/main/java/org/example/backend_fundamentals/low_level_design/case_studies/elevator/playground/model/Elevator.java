package org.example.backend_fundamentals.low_level_design.case_studies.elevator.playground.model;

import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class Elevator {
  private static final AtomicInteger ID_COUNTER = new AtomicInteger();

  private final int id;
  private int currentFloor;
  private DoorState doorState;
  private Direction currentDirection;
  private final TreeSet<Integer> upStops = new TreeSet<>();
  private final TreeSet<Integer> downStops = new TreeSet<>();

  public Elevator() {
    this.id = ID_COUNTER.incrementAndGet();
    this.currentFloor = 0;
    this.doorState = DoorState.CLOSED;
    this.currentDirection = Direction.STOP;
  }

  public void advanceOneStep() {

    if(this.doorState == DoorState.OPEN){
      this.doorState = DoorState.CLOSED;
      return;
    }

    // currently stopped
    if (this.currentDirection == Direction.STOP) {
      return;
    }

    if (this.currentDirection == Direction.UP) {
      advanceUp();
      return;
    }

    advanceDown();
  }

  private void advanceUp() {
    if(this.upStops.contains(currentFloor)) {
      this.doorState = DoorState.OPEN;
      upStops.remove(currentFloor);
      return;
    }
    if(upStops.isEmpty() && downStops.isEmpty()) {
      this.currentDirection = Direction.STOP;
      return;
    }
    if (upStops.higher(currentFloor) != null || (!downStops.isEmpty() && downStops.last()>currentFloor)) {
      currentFloor++;
      return;
    }
    this.currentDirection = Direction.DOWN;
  }

  private void advanceDown() {
    if(this.downStops.contains(currentFloor)) {
      this.doorState = DoorState.OPEN;
      downStops.remove(currentFloor);
      return;
    }
    if(upStops.isEmpty() && downStops.isEmpty()) {
      this.currentDirection = Direction.STOP;
      return;
    }
    if (downStops.lower(currentFloor) != null || (!upStops.isEmpty() && upStops.first()<currentFloor)){
      currentFloor--;
      return;
    }
    this.currentDirection = Direction.UP;
  }

  public void addFloorRequest(int floorNumber) {

    // Reject a cabin destination matching the current floor.
    if(floorNumber == this.currentFloor){
      throw new IllegalArgumentException("Destination cannot be the current floor");
    }

    if(floorNumber > currentFloor) {
      upStops.add(floorNumber);
    } else {
      downStops.add(floorNumber);
    }

    if(this.currentDirection == Direction.STOP) {
      if(floorNumber > currentFloor) {
        this.currentDirection = Direction.UP;
      } else {
        this.currentDirection = Direction.DOWN;
      }
    }
  }

  public void addHallRequest(int floorNumber, Direction direction) {
    if(direction == null || direction == Direction.STOP) {
      throw new IllegalArgumentException("Direction must be UP or DOWN for a hall request.");
    }

    if(direction==Direction.UP) {
      this.upStops.add(floorNumber);
    } else {
      this.downStops.add(floorNumber);
    }

    if(this.currentDirection == Direction.STOP) {
      if(this.currentFloor < floorNumber){
        this.currentDirection = Direction.UP;
      } else if (this.currentFloor > floorNumber) {
        this.currentDirection = Direction.DOWN;
      } else {
        this.currentDirection = direction;
      }
    }
  }
}
