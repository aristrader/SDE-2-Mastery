package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic;

import java.util.List;

/** Concrete product created by {@link BackendHiringProcess}. */
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
