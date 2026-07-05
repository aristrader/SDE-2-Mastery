package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic.playground;

/**
 * Entry point for the learning variant of Factory Method.
 *
 * <p>Notice how little wiring there is compared to the production variant: no services to
 * construct, no collaborators to pass in. That compactness is the whole point of this
 * variant.
 */
public class FactoryMethodRun {

  public static void main(String[] args) {
    HR hrForAndroid = new HR(new AndroidHiringProcess());
    HR hrForBackend = new HR(new BackendHiringProcess());
    HR hrForIos = new HR(new IosHiringProcess());

    hrForAndroid.hireForTeam();
    hrForBackend.hireForTeam();
    hrForIos.hireForTeam();
  }
}
