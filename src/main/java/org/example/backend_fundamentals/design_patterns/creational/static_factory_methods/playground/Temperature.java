package org.example.backend_fundamentals.design_patterns.creational.static_factory_methods.playground;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.ToString;

/**
 * Immutable temperature value, reachable only via static factory methods.
 * Demonstrates the two most common Effective Java Item 1 benefits:
 *
 * <ul>
 *   <li><b>Names disambiguate.</b> {@link #celsius(double)},
 *       {@link #fahrenheit(double)}, and {@link #kelvin(double)} all take a
 *       single {@code double} &mdash; no overloaded constructor could
 *       distinguish between them.
 *   <li><b>Instance control / caching.</b> All factories funnel through a
 *       private {@code ConcurrentHashMap} cache, so repeated calls with the
 *       same Celsius value return the <i>same</i> object.
 * </ul>
 *
 * <p>See {@code StaticFactoryMethods.md} for the full walkthrough.
 */
@ToString
public final class Temperature {

  private static final Map<Double, Temperature> CACHE = new ConcurrentHashMap<>();
  private static final Temperature ABSOLUTE_ZERO = cached(-273.15);

  private final double celsius;

  private Temperature(double celsius) {
    this.celsius = celsius;
  }

  public static Temperature celsius(double celsius) {
    return cached(celsius);
  }

  public static Temperature fahrenheit(double fahrenheit) {
    return cached((fahrenheit - 32.0) * 5.0 / 9.0);
  }

  public static Temperature kelvin(double kelvin) {
    if (kelvin < 0) {
      throw new IllegalArgumentException("kelvin must be >= 0, got: " + kelvin);
    }
    return cached(kelvin - 273.15);
  }

  /**
   * Parses a literal of the form {@code "<number><unit>"} where unit is
   * {@code C}, {@code F}, or {@code K}. Examples: {@code "20C"},
   * {@code "68F"}, {@code "293.15K"}.
   *
   * @throws IllegalArgumentException if the literal is malformed or the
   *     unit is not one of {@code C} / {@code F} / {@code K}
   */
  public static Temperature fromString(String literal) {
    if (literal == null || literal.length() < 2) {
      throw new IllegalArgumentException("malformed literal: " + literal);
    }
    char unit = literal.charAt(literal.length() - 1);
    double value;
    try {
      value = Double.parseDouble(literal.substring(0, literal.length() - 1));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("malformed literal: " + literal, e);
    }
    switch (unit) {
      case 'C': return celsius(value);
      case 'F': return fahrenheit(value);
      case 'K': return kelvin(value);
      default:
        throw new IllegalArgumentException("unknown unit '" + unit + "' in: " + literal);
    }
  }

  /** Cached singleton at {@code -273.15°C}. */
  public static Temperature absoluteZero() {
    return ABSOLUTE_ZERO;
  }

  public double asCelsius() {
    return celsius;
  }

  public double asFahrenheit() {
    return celsius * 9.0 / 5.0 + 32.0;
  }

  public double asKelvin() {
    return celsius + 273.15;
  }

  private static Temperature cached(double celsius) {
    return CACHE.computeIfAbsent(celsius, Temperature::new);
  }
}
