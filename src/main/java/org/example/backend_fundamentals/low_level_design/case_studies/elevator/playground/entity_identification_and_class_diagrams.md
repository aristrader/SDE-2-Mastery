# Elevator — entity identification and class diagrams

## Goal

Model the smallest useful in-memory elevator system that assigns hall requests and moves cars through
their pending stops.

This is the agreed scope after discussing the initial [interviewer prompt](../exercise/problem_statement/) and
[candidate clarifications](../exercise/candidate_discussion/).

## Requirements

- A building has a fixed positive floor count and two or more elevator cars.
- A hall request identifies an origin floor and direction. A cabin request identifies a destination floor
  for one assigned car.
- Each elevator tracks its identifier, current floor, current direction, door state, and pending stops.
- The controller assigns a hall request to the nearest eligible car. Prefer idle cars, then cars already
  travelling toward and able to pass the request floor; break ties by lower car identifier.
- An elevator may accept cabin destinations after it is assigned a hall request.
- A car moves one floor per simulation step toward a pending stop in its current direction.
- At a requested stop, the car serves the stop before continuing. It reverses only when no pending stop
  remains ahead in its current direction.
- Reject invalid floors, a hall request whose direction points outside the building, and a cabin request
  for the car's current floor.

## Constraints

- Keep all state in memory.
- Model movement deterministically; real-time clocks, threads, and persistence are not part of the base
  implementation.
- Use one explicit assignment rule; do not introduce a dispatch strategy hierarchy until a requirement
  needs multiple policies.

## Test scenarios

- Assign a hall request to the nearest idle elevator.
- Prefer an eligible elevator travelling toward the requested floor over one travelling away from it.
- Add a cabin stop and service it while continuing in the current direction.
- Reverse only after the car has no stop ahead in its current direction.
- Resolve equal assignment candidates using the lower car identifier.
- Reject invalid floors and invalid boundary directions.

## Interview follow-ups

- How would you support concurrent hall requests without assigning the same car inconsistently?
- How would you add capacity, overload, door-obstruction, emergency, and maintenance states?
- How would you evolve nearest-eligible assignment into a cost-based dispatch policy?
- What changes for multiple buildings, a central dispatcher, and persistent request state?
- How would you prevent starvation for a request repeatedly bypassed by moving cars?

## Entity identification
Elevator
Direction
DoorState
Elevator assigning strategy

## Class diagrams

```text

ElevatorService
- totalFloors : int
- elevators : List<Elevator>
+ advanceOneStep() : void // advances each elevator by one logical movement step
+ assignElevator(int requestFloor, Direction direction) : Elevator
+ addFloorStop(int elevatorId, int floor)

Elevator
- id : int
- currentFloor : int
- doorState : DoorState
- currentDirection : Direction
- upStops : TreeSet<Integer>
- downStops : TreeSet<Integer>
+ advanceOneStep() : void
+ addFloorRequest(int floorNumber) : void // place in upStops or downStops based on its relation to currentFloor
+ addHallRequest(int floorNumber, Direction direction) : void // hall calls , since direction would also matter

Direction (enum)
- UP
- DOWN
- STOP

DoorState (enum)
- open
- closed

// Follow-up only: the base scope has one fixed nearest-eligible assignment rule in ElevatorService.
// Extract this policy only when requirements introduce alternatives such as VIP, emergency, zoning, or
// cost-based dispatch. UP/DOWN is request and elevator state, not a reason to use Strategy by itself.
interface ElevatorAssigning
- assignElevator(int requestFloor, Direction direction) : Elevator

ClosestElevator implements ElevatorAssigning

ElevatorAssignerResolver
- resolve() :ElevatorAssigning
```
