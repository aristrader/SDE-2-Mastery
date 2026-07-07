---
order: 20
search: false
---

# Solutions

## Solution: is-a-relationship - The IS-A relationship

```java
class Vehicle {
    public void startEngine() {
        System.out.println("Engine started.");
    }
}

class Car extends Vehicle {
    // Inherits startEngine automatically
}

public class Main {
    public static void main(String[] args) {
        Car car = new Car();
        car.startEngine(); // Prints "Engine started."
    }
}
```

## Solution: super-keyword - Using super

```java
class Vehicle {
    protected String brand;

    public Vehicle(String brand) {
        this.brand = brand;
    }

    public void startEngine() {
        System.out.println(brand + " engine started.");
    }
}

class Car extends Vehicle {
    private int doors;

    public Car(String brand, int doors) {
        super(brand); // Must be the first statement
        this.doors = doors;
    }

    @Override
    public void startEngine() {
        System.out.println("Preparing car engine...");
        super.startEngine(); // Invokes parent logic
    }
}
```
