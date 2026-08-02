---
order: 50
---
# Composition vs Inheritance

## Why prefer composition over inheritance?
Inheritance models an **IS-A** relationship, which creates very strong, tight coupling. Composition models a **HAS-A** relationship (e.g., `Car` HAS-A `Engine`), which keeps responsibilities separate and loosely coupled.

The practical rule is:
- Use **inheritance** when the child is truly a more specific version of the parent and can safely replace it everywhere.
- Use **composition** when you want to reuse behavior, swap behavior, or combine behavior in different ways.

Inheritance is not bad. It is just expensive: once a subclass extends a parent, it inherits the parent's API, fields, assumptions, and future changes.

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

## Prefer composition for swappable behavior

Inheritance version:

```java
abstract class Discount {
    abstract int apply(int amount);
}

class FestivalDiscount extends Discount {
    int apply(int amount) {
        return amount - 100;
    }
}

class CheckoutService extends FestivalDiscount {
    int checkout(int amount) {
        return apply(amount);
    }
}
```

This is awkward because `CheckoutService` is not a discount. It only wants to **use** a discount.

Composition version:

```java
interface DiscountPolicy {
    int apply(int amount);
}

class FestivalDiscount implements DiscountPolicy {
    public int apply(int amount) {
        return amount - 100;
    }
}

class CheckoutService {
    private final DiscountPolicy discountPolicy;

    CheckoutService(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    int checkout(int amount) {
        return discountPolicy.apply(amount);
    }
}
```

Now checkout logic and discount logic are separate. Tests can pass a fake discount, production can pass a real one, and adding `NoDiscount` or `LoyaltyDiscount` does not require a new checkout subclass.

## Composition with components
Instead of deep inheritance, use composition to delegate behaviors to specific strategies or components.

```java
interface Engine {
    void start();
}

class Car {
    private final Engine engine;

    Car(Engine engine) {
        this.engine = engine;
    }

    void start() {
        engine.start();
    }
}
```

`Car` does not need to know whether the engine is electric, diesel, or mocked in a test. It just depends on the behavior it needs.

## When inheritance is still fine

Use inheritance when the hierarchy is stable and the subtype really is substitutable:

```java
abstract class Shape {
    abstract double area();
}

class Circle extends Shape {
    private final double radius;

    Circle(double radius) {
        this.radius = radius;
    }

    double area() {
        return Math.PI * radius * radius;
    }
}
```

`Circle` really is a `Shape`, and any code that asks for a `Shape` can safely call `area()`.

## Quick recall

- **Inheritance models?** IS-A.
- **Composition models?** HAS-A.
- **Why prefer composition often?** Less coupling, easier testing, and easier behavior swaps.
- **Classic inheritance smell?** Subclasses inherit irrelevant behavior/state.
- **Decision rule?** If you only want to reuse or swap behavior, compose. If the subtype truly replaces the parent, inheritance may be fine.
