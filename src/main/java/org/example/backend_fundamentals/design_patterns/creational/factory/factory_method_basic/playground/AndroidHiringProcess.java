package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic.playground;

/** Concrete creator &mdash; plugs an {@link AndroidDeveloper} into the hiring flow. */
public class AndroidHiringProcess extends DeveloperHiringProcess {

  @Override
  protected Employee createDeveloper() {
    return new AndroidDeveloper();
  }
}
