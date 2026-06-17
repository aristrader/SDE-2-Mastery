package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method;

/**
 * Client that delegates hiring to whichever {@link HiringProcess} it received at construction.
 * Depends only on the interface — never names a concrete developer type.
 */
public class HR {

  private final HiringProcess hiringProcess;

  // Package-private: wired by FactoryMethodRun in the same package. Widen to public for DI.
  HR(HiringProcess hiringProcess) {
    this.hiringProcess = hiringProcess;
  }

  /** @return the newly hired employee */
  public Employee hireForTeam() {
    return hiringProcess.onboard();
  }
}
