package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic.playground;

import java.util.List;

/** Concrete product created by {@link AndroidHiringProcess}. */
class AndroidDeveloper implements Employee {

  AndroidDeveloper() {
    System.out.println("Hi i am a android developer joining.");
  }

  @Override
  public int getSalary() {
    return 10000;
  }

  @Override
  public List<String> getTechStack() {
    return List.of("Kotlin", "Java", "Android SDK");
  }

  @Override
  public String getTitle() {
    return "Android Developer";
  }
}
