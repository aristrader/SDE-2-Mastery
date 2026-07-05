package org.example.backend_fundamentals.design_patterns.creational.factory.simple_factory.playground;

import java.util.List;

/**
 * Concrete product created by {@link DeveloperSimpleFactory}. The package-private constructor
 * nudges callers to go through the factory rather than instantiating directly.
 */
public class AndroidDeveloper implements Employee {

  AndroidDeveloper() {
    System.out.println("Hi i am a android developer joining.");
  }

  @Override
  public int getSalary() {
    return 10000;
  }

  @Override
  public List<String> getTechStack() {
    return List.of("HTML", "CSS", "JAVASCRIPT");
  }
}
