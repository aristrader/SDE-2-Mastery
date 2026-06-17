package org.example.backend_fundamentals.design_patterns.creational.prototype.simple;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Deep-copy prototype demo using a copy constructor.
 *
 * <p>The copy constructor streams through each {@link Point} and constructs a brand-new
 * instance for every entry. Original and copy share no mutable state — mutating a point
 * in the copy cannot affect the original.
 */
@Getter
@Setter
@ToString
public class ShapeDeep {

  private String name;
  private List<Point> points;

  /** Primary constructor. {@code points} is stored as-is — no defensive copy (intentional for demo). */
  public ShapeDeep(String name, List<Point> points){
    this.name = name;
    this.points = points;
  }

  // new ArrayList<>(shape.points) looks like a deep copy but is not —
  // it copies the list wrapper, not the Point objects inside. Both lists still hold
  // references to the same Point instances, so setX/setY on one affects the other.
//  public ShapeDeep(ShapeDeep shape) {
//    this.name = shape.name;
//    this.points = new ArrayList<>(shape.points);
//  }

  /** Deep copy — each {@link Point} is reconstructed so no mutable state is shared with the original. */
  public ShapeDeep(ShapeDeep shape) {
    this.name = shape.name;
    this.points = shape.getPoints().stream()
        .map(point -> new Point(point.getX(), point.getY()))
        .collect(Collectors.toList());
  }
}
