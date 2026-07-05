package org.example.backend_fundamentals.design_patterns.creational.prototype.playground.simple;

import java.util.List;


/**
 * Demo runner contrasting shallow copy ({@link ShapeShallow}) with deep copy ({@link ShapeDeep}).
 *
 * <p>Both sections mutate point[2] on the copy, then print original and copy side-by-side.
 * In the shallow section both lines change; in the deep section only the copy changes.
 */
public class ShapeRun {

  public static void main(String[] args) {
    // Shallow copy
    System.out.println("SHALLOW");
    ShapeShallow shallow = new ShapeShallow("Rectangle",
        List.of(new Point(1,1), new Point(2,2), new Point(3,3), new Point(4,4)));
    ShapeShallow shallowCopy = new ShapeShallow(shallow);
    shallowCopy.getPoints().get(2).setX(5);
    shallowCopy.getPoints().get(2).setY(5);
    System.out.println(shallow);
    System.out.println(shallowCopy);

    System.out.println("DEEP");
    ShapeDeep deep = new ShapeDeep("Rectangle",
        List.of(new Point(1,1), new Point(2,2), new Point(3,3), new Point(4,4)));
    ShapeDeep deepCopy = new ShapeDeep(deep);
    deepCopy.getPoints().get(2).setX(5);
    deepCopy.getPoints().get(2).setY(5);
    System.out.println(deep);
    System.out.println(deepCopy);
  }
}
