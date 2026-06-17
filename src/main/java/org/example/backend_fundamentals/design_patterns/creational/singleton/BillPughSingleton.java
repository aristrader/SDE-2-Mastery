package org.example.backend_fundamentals.design_patterns.creational.singleton;

/**
 * Singleton using the initialization-on-demand holder idiom.
 *
 * <p>{@link SingletonHelper} is not loaded until {@link #getInstance()} is first called, giving
 * lazy, lock-free thread safety via JVM class-init guarantees. Preferred over
 * {@link ThreadSafeSingleton} because it avoids {@code synchronized} entirely.
 */
public class BillPughSingleton {

  private BillPughSingleton() {}

  private static class SingletonHelper {
    private static final BillPughSingleton INSTANCE = new BillPughSingleton();
  }

  /** @return the singleton instance, created lazily on first call */
  public static BillPughSingleton getInstance() {
    return SingletonHelper.INSTANCE;
  }

  public static void main(String[] args) {

    BillPughSingleton obj3 = BillPughSingleton.getInstance();
    BillPughSingleton obj4 = BillPughSingleton.getInstance();
    if (obj3.equals(obj4)) {
      System.out.println("SAME");
    } else {
      System.out.println("DIFFERENT");
    }
  }
}
