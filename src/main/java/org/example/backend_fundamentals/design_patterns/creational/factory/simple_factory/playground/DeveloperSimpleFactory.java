package org.example.backend_fundamentals.design_patterns.creational.factory.simple_factory.playground;

import lombok.experimental.UtilityClass;

/**
 * Simple Factory implementation.
 *
 * <p>Exposes a single static method, {@link #getDeveloper(Developer)}, that maps a discriminator
 * enum value to a concrete {@link Employee} instance. This is the textbook "Simple Factory"
 * (also called "Static Factory"); it is <b>not</b> the Gang of Four Factory Method pattern,
 * which uses polymorphism (abstract creator + concrete creator subclasses) rather than
 * {@code if}/{@code else} on a parameter.
 *
 * <p>Simple Factory is easy to write and read, but adding a new product type forces a change
 * to this class &mdash; a violation of the Open/Closed Principle. When that cost matters,
 * promote the design to GoF Factory Method.
 */
@UtilityClass
public class DeveloperSimpleFactory {

  public static Employee getDeveloper(Developer developer) {
    if (developer.equals(Developer.ANDROID_DEVELOPER)) {
      return new AndroidDeveloper();
    } else if (developer.equals(Developer.BACKEND_DEVELOPER)) {
      return new BackendDeveloper();
    } else {
      return null;
    }
  }
}
