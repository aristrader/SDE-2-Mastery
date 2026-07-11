package org.example.backend_fundamentals.java.oop.pillars.inheritance.playground;

/**
 * The canonical LSP violation — {@code Square extends Rectangle} — and a
 * clean fix using a shared {@code Shape} interface.
 * <p>
 * Mathematically a square <em>is</em> a rectangle, so the English "is-a" test
 * passes. But behaviourally, {@code Square.setWidth(w)} must also change height
 * to preserve the square invariant, while {@code Rectangle.setWidth(w)} promises
 * to change <em>only</em> width. Any caller written against {@code Rectangle}
 * is silently surprised when handed a {@code Square}: the substitution broke.
 * <p>
 * The fix is to drop the inheritance and have both shapes implement a common
 * abstract type ({@code Shape}). Neither pretends to be the other; both satisfy
 * the only contract callers actually need ({@code area()}).
 */
public class LiskovSquareRectangle {

  // =========== Wrong way: Square extends Rectangle ===========

  static class Rectangle {
    protected int width, height;

    public void setWidth(int w)  { this.width = w; }
    public void setHeight(int h) { this.height = h; }
    public int area() { return width * height; }
  }

  static class Square extends Rectangle {
    @Override
    public void setWidth(int w)  { this.width = w; this.height = w; }   // also touches height
    @Override
    public void setHeight(int h) { this.width = h; this.height = h; }
  }

  /** Caller written against the Rectangle contract: setWidth only touches width. */
  static void doubleWidth(Rectangle r) {
    int originalHeight = r.height;
    r.setWidth(r.width * 2);
    System.out.println("  After doubleWidth: width=" + r.width + ", height=" + r.height
        + "  (expected height to remain " + originalHeight + ")");
  }

  // =========== Right way: both implement Shape ===========

  interface Shape {
    int area();
  }

  static class ImmutableRectangle implements Shape {
    private final int width, height;
    public ImmutableRectangle(int w, int h) { this.width = w; this.height = h; }
    @Override public int area() { return width * height; }
  }

  static class ImmutableSquare implements Shape {
    private final int side;
    public ImmutableSquare(int side) { this.side = side; }
    @Override public int area() { return side * side; }
  }

  public static void main(String[] args) {
    System.out.println("=== Wrong: Square extends Rectangle ===");
    System.out.println("Rectangle 3x4:");
    Rectangle rect = new Rectangle();
    rect.setWidth(3);
    rect.setHeight(4);
    doubleWidth(rect);   // height stays 4 — works as expected

    System.out.println("Square 3x3 (substituted in as Rectangle):");
    Rectangle square = new Square();
    square.setWidth(3);
    square.setHeight(3);
    doubleWidth(square); // height silently doubled — LSP failure

    System.out.println();
    System.out.println("=== Right: both implement Shape ===");
    Shape[] shapes = { new ImmutableRectangle(3, 4), new ImmutableSquare(3) };
    for (Shape s : shapes) {
      System.out.println("  " + s.getClass().getSimpleName() + " area = " + s.area());
    }
    // Both work uniformly under Shape; neither pretends to be the other.
  }
}
