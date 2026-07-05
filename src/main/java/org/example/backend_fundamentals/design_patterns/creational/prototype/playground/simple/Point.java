package org.example.backend_fundamentals.design_patterns.creational.prototype.playground.simple;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mutable coordinate used by the prototype demos.
 *
 * <p>Mutability is intentional — it makes the shallow-copy trap visible. Both original and
 * shallow copy hold references to the same {@code Point} instances, so calling
 * {@code setX}/{@code setY} on either is seen by both.
 */
@Getter
@Setter
@ToString
public class Point {
  private int x;
  private int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
