package org.example.backend_fundamentals.design_patterns.creational.static_factory_methods.playground;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo runner for {@link Temperature}, walking the two Effective Java Item 1
 * benefits the class demonstrates: named factories that disambiguate, and
 * instance control via caching.
 */
@Slf4j
public class TemperatureRun {

  public static void main(String[] args) {
    log.info("=== Benefit #1: named factories disambiguate where overloaded ctors can't ===");
    Temperature roomC = Temperature.celsius(20);
    Temperature roomF = Temperature.fahrenheit(68);
    Temperature roomK = Temperature.kelvin(293.15);
    System.out.println("celsius(20)     => " + roomC);
    System.out.println("fahrenheit(68)  => " + roomF);
    System.out.println("kelvin(293.15)  => " + roomK);
    System.out.println("All three encode 20.0C: "
        + roomC.asCelsius() + ", "
        + roomF.asCelsius() + ", "
        + roomK.asCelsius());

    log.info("=== Benefit #1: 'from' convention for type conversion ===");
    Temperature parsed = Temperature.fromString("100C");
    System.out.println("fromString(\"100C\") => " + parsed);
    System.out.println("  in Fahrenheit:    " + parsed.asFahrenheit());

    log.info("=== Benefit #2: instance control / caching ===");
    Temperature a = Temperature.celsius(20);
    Temperature b = Temperature.celsius(20);
    Temperature c = Temperature.fahrenheit(68);
    System.out.println("celsius(20) == celsius(20)?     " + (a == b));
    System.out.println("celsius(20) == fahrenheit(68)?  " + (a == c) + "  (true because 68F == 20C)");

    Temperature zero1 = Temperature.absoluteZero();
    Temperature zero2 = Temperature.absoluteZero();
    System.out.println("absoluteZero() == absoluteZero()? " + (zero1 == zero2));

    log.info("=== Validation: invalid input rejected ===");
    try {
      Temperature.kelvin(-1);
      log.error("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      System.out.println("kelvin(-1) correctly rejected: " + e.getMessage());
    }
    try {
      Temperature.fromString("20X");
      log.error("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      System.out.println("fromString(\"20X\") correctly rejected: " + e.getMessage());
    }
  }
}
