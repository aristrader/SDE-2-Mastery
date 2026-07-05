package org.example.backend_fundamentals.design_patterns.creational.factory.simple_factory;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Entry point that demonstrates {@link DeveloperSimpleFactory} by asking it for each supported
 * developer type and printing the resulting {@link Employee}'s salary and tech stack.
 */
@Slf4j
public class SimpleFactoryRun {

  public static void main(String[] args) {
    Employee e1 = DeveloperSimpleFactory.getDeveloper(Developer.ANDROID_DEVELOPER);
    log.info(
        "Employee has a salary of : {} and knows techStack {}",
        e1 != null ? e1.getSalary() : 0,
        e1 != null ? e1.getTechStack() : List.of());

    Employee e2 = DeveloperSimpleFactory.getDeveloper(Developer.BACKEND_DEVELOPER);
    log.info(
        "Employee has a salary of : {} and knows techStack {}",
        e2 != null ? e2.getSalary() : 0,
        e2 != null ? e2.getTechStack() : List.of());
  }
}
