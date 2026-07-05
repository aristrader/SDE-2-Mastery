package org.example.backend_fundamentals.design_patterns.creational.abstract_factory.playground;

import lombok.ToString;

@ToString
public class LuxurySofa implements Sofa {

  private final String material = "Luxury material";
  private final String color = "Shinny color";

  @Override
  public String getMaterial() {
    return material;
  }

  @Override
  public String getColor() {
    return color;
  }
}
