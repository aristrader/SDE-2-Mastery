package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method;

import java.util.List;

/**
 * Concrete product created by {@link BackendHiringProcess#createDeveloper()}. Package-private
 * so that clients have to go through a concrete creator rather than instantiating directly.
 */
class BackendDeveloper implements Employee {

  BackendDeveloper() {
    System.out.println("Hi i am a backend developer joining.");
  }

  @Override
  public int getSalary() {
    return 20000;
  }

  @Override
  public List<String> getTechStack() {
    return List.of("Java", "SQL", "Spring");
  }

  @Override
  public String getTitle() {
    return "Backend Developer";
  }
}
