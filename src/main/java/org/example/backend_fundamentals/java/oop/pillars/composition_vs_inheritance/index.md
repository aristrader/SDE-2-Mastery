---
order: 50
---
# Composition vs Inheritance

## Why prefer composition over inheritance?
Inheritance models an **IS-A** relationship, which creates very strong, tight coupling. Composition models a **HAS-A** relationship (e.g., `Car` HAS-A `Engine`), which keeps responsibilities separate and loosely coupled.

## When Inheritance Becomes Brittle

### 1. Parent Class Changes
If `Vehicle` gets a `fuelCapacity` field, subclasses like `ElectricCar` now inherit a meaningless field. The hierarchy no longer models reality.

### 2. The Penguin Problem (LSP Violation)
```java
class Bird { void fly() {} }
class Penguin extends Bird { void fly() { throw new UnsupportedOperationException(); } }
```
This violates the **Liskov Substitution Principle (LSP)** because a `Penguin` cannot truly replace a `Bird` everywhere if code assumes every bird can fly.

### 3. Combinatorial Explosion
If vehicles can be Electric, Diesel, Autonomous, or Flyable, inheritance creates an explosion of subclasses (`ElectricAutonomousCar`, `DieselFlyableCar`).

## The Solution
Instead of deep inheritance, use composition to delegate behaviors to specific strategies or components.
```java
class Car {
    private Engine engine; // ElectricMotor, DieselEngine, etc.
}
```

## Quick recall

- **Inheritance models?** IS-A.
- **Composition models?** HAS-A.
- **Why prefer composition often?** Less coupling and easier behavior swaps.
- **Classic inheritance smell?** Subclasses inherit irrelevant behavior/state.
