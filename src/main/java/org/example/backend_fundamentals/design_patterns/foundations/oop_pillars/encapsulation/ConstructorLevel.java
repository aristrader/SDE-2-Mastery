package org.example.backend_fundamentals.design_patterns.foundations.oop_pillars.encapsulation;

/**
 * Encapsulation enforced at construction time.
 * <p>
 * The field is {@code final} and there is no setter. The only place the invariant
 * (age must be non-negative) is checked is the constructor — so once an instance
 * exists, it is guaranteed to be in a valid state for its whole lifetime.
 */
public class ConstructorLevel {

  private final int age;

  public ConstructorLevel(int age) {
    if (age < 0) {
      throw new IllegalArgumentException("Age cannot be negative");
    }
    this.age = age;
  }

  public int getAge() {
    return age;
  }

  public static void main(String[] args) {
    try {
      ConstructorLevel adult = new ConstructorLevel(25);
      System.out.println("Created successfully with age " + adult.getAge());
    } catch (Exception e) {
      System.out.println("Failed to create with valid age");
    }

    try {
      ConstructorLevel invalid = new ConstructorLevel(-1);
      System.out.println("Created successfully with age " + invalid.getAge());
    } catch (Exception e) {
      System.out.println("Failed to create with negative age: " + e.getMessage());
    }
  }
}
