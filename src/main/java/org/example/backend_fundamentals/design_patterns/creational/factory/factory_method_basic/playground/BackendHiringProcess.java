package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic.playground;

/** Concrete creator &mdash; plugs a {@link BackendDeveloper} into the hiring flow. */
public class BackendHiringProcess extends DeveloperHiringProcess {

  @Override
  protected Employee createDeveloper() {
    return new BackendDeveloper();
  }
}
