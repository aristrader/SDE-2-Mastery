---
order: 20
search: false
---

# Solutions

## Solution: brittle-hierarchy - The Penguin Problem

```java
class Bird {
    public void fly() { System.out.println("Flying high!"); }
}

class Penguin extends Bird {
    @Override
    public void fly() { throw new UnsupportedOperationException("Penguins can't fly"); }
}

public class Main {
    public static void main(String[] args) {
        List<Bird> birds = List.of(new Bird(), new Penguin());
        for (Bird b : birds) {
            b.fly(); // Crashes on the second iteration
        }
    }
}
```
This violates the Liskov Substitution Principle because a `Penguin` cannot truly replace a `Bird`. If a caller expects a `Bird` to be able to fly, passing a `Penguin` breaks the caller's assumption.

## Solution: refactor-to-composition - Refactoring to HAS-A

```java
interface FlyBehavior { void fly(); }
class FlyWithWings implements FlyBehavior { public void fly() { System.out.println("Flying"); } }
class NoFly implements FlyBehavior { public void fly() { System.out.println("Can't fly"); } }

class Bird {
    private final FlyBehavior flyBehavior;
    public Bird(FlyBehavior flyBehavior) { this.flyBehavior = flyBehavior; }
    public void performFly() { flyBehavior.fly(); }
}

class Eagle extends Bird {
    public Eagle() { super(new FlyWithWings()); }
}

class Penguin extends Bird {
    public Penguin() { super(new NoFly()); }
}

public class Main {
    public static void main(String[] args) {
        List<Bird> birds = List.of(new Eagle(), new Penguin());
        birds.forEach(Bird::performFly); // No crash!
    }
}
```
By switching from IS-A to HAS-A, we decouple the flying capability from the `Bird` identity. We no longer throw unexpected exceptions; instead, we safely execute the composed behavior.
