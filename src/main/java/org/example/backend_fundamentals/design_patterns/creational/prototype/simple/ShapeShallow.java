package org.example.backend_fundamentals.design_patterns.creational.prototype.simple;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Shallow-copy prototype demo using a copy constructor.
 *
 * <p>The copy constructor assigns {@code points} by reference — original and copy share
 * the same list and the same {@link Point} objects. Mutating a point in the copy also
 * mutates the original; that is the shallow-copy trap this class is meant to show.
 */
@Getter
@Setter
@ToString
public class ShapeShallow {

  private String name;
  private List<Point> points;

  /** Primary constructor. {@code points} is stored as-is — no defensive copy (intentional for demo). */
  public ShapeShallow(String name, List<Point> points){
    this.name = name;
    this.points = points;
  }

  /** Shallow copy — {@code points} reference is shared with the original, not duplicated. */
  public ShapeShallow(ShapeShallow shape) {
    this.name = shape.name;
    this.points = shape.points;
  }
}
