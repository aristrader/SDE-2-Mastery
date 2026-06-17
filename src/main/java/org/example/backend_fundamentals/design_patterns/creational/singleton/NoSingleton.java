package org.example.backend_fundamentals.design_patterns.creational.singleton;

/**
 * Counter-example — not a singleton. Every {@code new NoSingleton(...)} produces a distinct
 * object, illustrating why the pattern is needed when a single shared instance is required.
 */
public class NoSingleton {

  int x;

  NoSingleton(int value) {
    x = value;
  }

  public static void main(String[] args) {

    NoSingleton obj1 = new NoSingleton(5);
    NoSingleton obj2 = new NoSingleton(5);
    if (obj1.equals(obj2)) {
      System.out.println("SAME");
    } else {
      System.out.println("DIFFERENT");
    }
  }
}
