---
order: 20
search: false
---

# Solutions

## Solution: method-overriding - Runtime polymorphism

```java
abstract class Animal {
    public abstract void makeSound();
}

class Dog extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Woof");
    }
}

class Cat extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Meow");
    }
}

public class Main {
    public static void main(String[] args) {
        List<Animal> animals = List.of(new Dog(), new Cat());
        // Dynamic dispatch: The actual object type determines which method runs
        for (Animal a : animals) {
            a.makeSound(); 
        }
    }
}
```

## Solution: method-overloading - Compile-time polymorphism

```java
class Calculator {
    // Overload 1
    public int add(int a, int b) {
        return a + b;
    }

    // Overload 2
    public int add(int a, int b, int c) {
        return a + b + c;
    }
}

public class Main {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        System.out.println(calc.add(2, 3));       // Calls Overload 1
        System.out.println(calc.add(2, 3, 4));    // Calls Overload 2
    }
}
```
