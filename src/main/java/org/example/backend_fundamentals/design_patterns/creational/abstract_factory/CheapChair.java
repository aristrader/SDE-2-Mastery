package org.example.backend_fundamentals.design_patterns.creational.abstract_factory;

import lombok.ToString;

@ToString
public class CheapChair implements Chair {

  private final String material = "Cheap material";
  private final String color = "Faded color";

  @Override
  public String getMaterial() {
    return material;
  }

  @Override
  public String getColor() {
    return color;
  }
}
