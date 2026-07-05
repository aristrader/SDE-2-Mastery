package org.example.backend_fundamentals.java.oop.polymorphism.playground;

import java.util.List;

/**
 * Polymorphism that works — instance-method dispatch.
 * <p>
 * Calling {@code speak()} on a {@code List<Animal>} dispatches to each concrete
 * animal's override at runtime. The declared type {@code Animal} is just the
 * lookup starting point; the actual object's class wins.
 */
public class WorkingPolymorphism {

  static abstract class Animal {
    abstract String speak();
  }

  static class Dog extends Animal {
    @Override
    String speak() { return "Woof"; }
  }

  static class Cat extends Animal {
    @Override
    String speak() { return "Meow"; }
  }

  public static void main(String[] args) {
    List<Animal> animals = List.of(new Dog(), new Cat());
    for (Animal a : animals) {
      // invokevirtual Animal.speak() — JVM picks Dog.speak() or Cat.speak() at runtime.
      System.out.println(a.getClass().getSimpleName() + " says: " + a.speak());
    }
  }
}
