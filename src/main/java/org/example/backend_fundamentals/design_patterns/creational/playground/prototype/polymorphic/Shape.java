package org.example.backend_fundamentals.design_patterns.creational.prototype.polymorphic;

import lombok.Getter;
import lombok.ToString;

/**
 * Abstract prototype. Declares the {@link #clone()} contract every concrete shape must honour.
 *
 * <p>The copy constructor (the {@code protected Shape(Shape source)} overload) holds the fields
 * shared by all shapes. Concrete subclasses call {@code super(source)} to copy them, then copy
 * their own fields — keeping the parent's copy logic in one place.
 *
 * <p>Callers work only against this type and call {@code shape.clone()}. The JVM dispatches to
 * the correct concrete {@code clone()} at runtime — the caller never checks {@code instanceof}
 * or casts.
 */
@Getter
@ToString
public abstract class Shape {

  protected int x;
  protected int y;
  protected String color;

  public Shape(int x, int y, String color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }

  /** Copy constructor — copies shared fields. Called by concrete subclasses via {@code super(source)}. */
  protected Shape(Shape source) {
    this.x = source.x;
    this.y = source.y;
    this.color = source.color;
  }

  /**
   * Returns an independent deep copy of this shape.
   *
   * <p>Each concrete subclass overrides this with a covariant return type (e.g., {@code Circle})
   * so callers that know the concrete type getValue it back without a cast.
   */
  public abstract Shape clone();
}
