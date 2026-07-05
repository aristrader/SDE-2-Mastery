package org.example.backend_fundamentals.design_patterns.creational.prototype.playground.polymorphic;

import lombok.Getter;
import lombok.ToString;

/**
 * Concrete prototype — a rectangle. Overrides {@link Shape#clone()} with a covariant return
 * type so callers who already know they have a {@code Rectangle} getValue one back without a cast.
 */
@Getter
@ToString(callSuper = true)
public class Rectangle extends Shape {

  private int width;
  private int height;

  public Rectangle(int x, int y, String color, int width, int height) {
    super(x, y, color);
    this.width = width;
    this.height = height;
  }

  /** Private copy constructor — only {@link #clone()} should call this. */
  private Rectangle(Rectangle source) {
    super(source);
    this.width = source.width;
    this.height = source.height;
  }

  /** Returns an independent copy of this rectangle. */
  @Override
  public Rectangle clone() {
    return new Rectangle(this);
  }
}
