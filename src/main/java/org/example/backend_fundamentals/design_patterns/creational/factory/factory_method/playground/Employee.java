package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method.playground;

import java.util.List;

/**
 * Product interface produced by concrete subclasses of {@link DeveloperHiringProcess}.
 *
 * <p>In the GoF Factory Method pattern the client depends only on this abstraction &mdash; it
 * never names a concrete type such as {@link AndroidDeveloper}, {@link BackendDeveloper}, or
 * {@link IosDeveloper}.
 */
public interface Employee {

  /** Monthly salary for this employee, in the concrete role's currency. */
  int getSalary();

  /** The technologies this employee is expected to work with. */
  List<String> getTechStack();

  /** Human-readable role title, e.g. {@code "Android Developer"}. */
  String getTitle();
}
