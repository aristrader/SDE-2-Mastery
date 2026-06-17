package org.example.backend_fundamentals.design_patterns.creational.prototype.polymorphic;

import lombok.Getter;
import lombok.ToString;

/**
 * Concrete prototype — a circle. Overrides {@link Shape#clone()} with a covariant return type
 * so callers who already know they have a {@code Circle} getValue one back without a cast.
 */
@Getter
@ToString(callSuper = true)
public class Circle extends Shape {

  private int radius;

  public Circle(int x, int y, String color, int radius) {
    super(x, y, color);
    this.radius = radius;
  }

  /** Private copy constructor — only {@link #clone()} should call this. */
  private Circle(Circle source) {
    super(source);
    this.radius = source.radius;
  }

  /** Returns an independent copy of this circle. */
  @Override
  public Circle clone() {
    return new Circle(this);
  }
}
