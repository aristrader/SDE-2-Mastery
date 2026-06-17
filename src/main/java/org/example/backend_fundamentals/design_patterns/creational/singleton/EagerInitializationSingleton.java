package org.example.backend_fundamentals.design_patterns.creational.singleton;

/**
 * Singleton using eager initialization — instance created at class-loading time.
 *
 * <p>Thread-safe via JVM static-init guarantees, but pays the construction cost even if the
 * instance is never used. Prefer {@link BillPughSingleton} for expensive or rarely-used singletons.
 */
public class EagerInitializationSingleton {

  private static final EagerInitializationSingleton obj = new EagerInitializationSingleton();

  private EagerInitializationSingleton() {}

  /** @return the eagerly created singleton instance */
  public static EagerInitializationSingleton getInstance() {
    return obj;
  }

  public static void main(String[] args) {

    EagerInitializationSingleton obj3 = EagerInitializationSingleton.getInstance();
    EagerInitializationSingleton obj4 = EagerInitializationSingleton.getInstance();
    if (obj3.equals(obj4)) {
      System.out.println("SAME");
    } else {
      System.out.println("DIFFERENT");
    }
  }
}
