package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic.playground;

/**
 * Client &mdash; hires a developer via a {@link DeveloperHiringProcess}.
 *
 * <p>Learning variant: since there is only one hiring flow in this package, {@code HR} binds
 * directly to {@code DeveloperHiringProcess}. In the production variant, this dependency is
 * on an interface ({@code HiringProcess}) so the same {@code HR} can accept developer, sales,
 * or other future flows.
 */
public class HR {

  private final DeveloperHiringProcess hiringProcess;

  HR(DeveloperHiringProcess hiringProcess) {
    this.hiringProcess = hiringProcess;
  }

  public Employee hireForTeam() {
    return hiringProcess.onboard();
  }
}
