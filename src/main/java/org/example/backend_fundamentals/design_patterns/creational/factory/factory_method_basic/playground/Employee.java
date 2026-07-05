package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic.playground;

import java.util.List;

/**
 * Product interface produced by the hiring flows in this package.
 *
 * <p>Same shape as the production variant &mdash; this package is the learning-focused,
 * intentionally simpler cousin of {@code factory_method/}.
 */
public interface Employee {

  int getSalary();

  List<String> getTechStack();

  String getTitle();
}
