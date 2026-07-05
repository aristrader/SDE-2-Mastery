package org.example.backend_fundamentals.design_patterns.creational.factory.simple_factory;

import java.util.List;

/**
 * Product interface returned by {@link DeveloperSimpleFactory}. All concrete developer types
 * implement this interface so that callers of the factory can work against a single abstraction.
 */
public interface Employee {

  int getSalary();

  List<String> getTechStack();
}
