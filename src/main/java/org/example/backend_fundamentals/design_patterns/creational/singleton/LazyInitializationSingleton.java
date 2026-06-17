package org.example.backend_fundamentals.design_patterns.creational.singleton;

/**
 * Singleton with lazy initialization — instance created on first {@link #getInstance()} call.
 *
 * <p><b>Not thread-safe.</b> Concurrent first-time callers may each create a distinct instance.
 * See {@link ThreadSafeSingleton} or {@link BillPughSingleton} for thread-safe variants.
 */
public class LazyInitializationSingleton {

  private static LazyInitializationSingleton obj;

  int x;

  private LazyInitializationSingleton() {}

  /** @return the singleton instance, created on first access (not thread-safe) */
  public static LazyInitializationSingleton getInstance() {
    if (obj == null) {
      obj = new LazyInitializationSingleton();
    }
    return obj;
  }

  public static void main(String[] args) {

    LazyInitializationSingleton obj3 = LazyInitializationSingleton.getInstance();
    LazyInitializationSingleton obj4 = LazyInitializationSingleton.getInstance();
    if (obj3.equals(obj4)) {
      System.out.println("SAME");
    } else {
      System.out.println("DIFFERENT");
    }
  }
}
