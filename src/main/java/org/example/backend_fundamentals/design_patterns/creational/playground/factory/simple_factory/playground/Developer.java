package org.example.backend_fundamentals.design_patterns.creational.factory.simple_factory;

/**
 * Discriminator enum used by {@link DeveloperSimpleFactory#getDeveloper(Developer)} to choose
 * which concrete {@link Employee} to instantiate. In Simple Factory, the caller passes in a
 * value from this enum and the factory decides the concrete type with an {@code if}/{@code else}.
 */
public enum Developer {
  ANDROID_DEVELOPER,
  BACKEND_DEVELOPER;
}
