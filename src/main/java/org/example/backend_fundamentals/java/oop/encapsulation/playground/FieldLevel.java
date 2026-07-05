package org.example.backend_fundamentals.java.oop.encapsulation.playground;

/**
 * Encapsulation enforced by a validating setter.
 * <p>
 * The field is {@code private}, so external code cannot assign it directly.
 * The only way in is through {@link #setPositiveNumber(int)}, which checks the
 * invariant. The matching {@link #getPositiveNumber()} lets callers <em>read</em>
 * the value but never <em>write</em> it without going through the validator.
 */
public class FieldLevel {

  private int positiveNumber;

  public void setPositiveNumber(int num) {
    if (num <= 0) {
      throw new IllegalArgumentException("Not a positive number: " + num);
    }
    this.positiveNumber = num;
  }

  public int getPositiveNumber() {
    return positiveNumber;
  }

  public static void main(String[] args) {
    FieldLevel fieldLevel = new FieldLevel();

    try {
      fieldLevel.setPositiveNumber(5);
      System.out.println("Set successfully — current value: " + fieldLevel.getPositiveNumber());
    } catch (Exception e) {
      System.out.println("Failed to set positive value: " + e.getMessage());
    }

    try {
      fieldLevel.setPositiveNumber(-5);
      System.out.println("Set successfully — current value: " + fieldLevel.getPositiveNumber());
    } catch (Exception e) {
      System.out.println("Failed to set negative value: " + e.getMessage());
    }
  }
}
