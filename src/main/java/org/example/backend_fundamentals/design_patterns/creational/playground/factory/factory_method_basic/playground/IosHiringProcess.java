package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic;

/** Concrete creator &mdash; plugs an {@link IosDeveloper} into the hiring flow. */
public class IosHiringProcess extends DeveloperHiringProcess {

  @Override
  protected Employee createDeveloper() {
    return new IosDeveloper();
  }
}
