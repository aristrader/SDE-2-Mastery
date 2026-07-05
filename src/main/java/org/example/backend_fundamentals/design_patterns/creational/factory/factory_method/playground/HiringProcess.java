package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method.playground;

/**
 * Contract implemented by every hiring flow in this package.
 *
 * <p>Only worth adding when multiple distinct flow types exist (developer, sales, designer).
 * Do not add this layer for a single flow.
 */
public interface HiringProcess {

  /** @return the newly hired employee */
  Employee onboard();
}
