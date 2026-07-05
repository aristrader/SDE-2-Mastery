package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method.playground;

import java.util.List;

/**
 * Concrete product created by {@link IosHiringProcess#createDeveloper()}. Package-private
 * so that clients go through the creator rather than instantiating directly.
 */
class IosDeveloper implements Employee {

  IosDeveloper() {
    System.out.println("Hi i am an iOS developer joining.");
  }

  @Override
  public int getSalary() {
    return 15000;
  }

  @Override
  public List<String> getTechStack() {
    return List.of("Swift", "Objective-C", "iOS SDK");
  }

  @Override
  public String getTitle() {
    return "iOS Developer";
  }
}
