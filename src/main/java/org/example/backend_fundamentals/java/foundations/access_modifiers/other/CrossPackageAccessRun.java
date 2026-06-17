package org.example.backend_fundamentals.java.foundations.access_modifiers.other;

/**
 * Runner for the three cross-package {@code protected}-access scenarios.
 *
 * <p>This runner exercises whatever DOES compile in each scenario. The compile-error cases
 * are kept as commented lines inside {@link OtherPackageRunner} and {@link OtherSubclass} so
 * you can experiment by uncommenting them and watching the compiler reject them.</p>
 *
 * <p>The lesson the output reinforces: {@code protected} from a different package is much
 * stricter than "you're a subclass, so you have access" — see the class JavaDocs for the
 * full two-condition rule.</p>
 */
public class CrossPackageAccessRun {

    public static void main(String[] args) {
        System.out.println("=== Scenario A: unrelated class in different package ===");
        new OtherPackageRunner().demo();

        System.out.println();
        System.out.println("=== Scenario C: subclass in different package — own-type access ===");
        OtherSubclass other = new OtherSubclass();
        other.scenarioC_ownTypeAccess();

        System.out.println();
        System.out.println("=== Scenario B: subclass — but wrong reference type ===");
        other.scenarioB_siblingThroughParentType();

        System.out.println();
        System.out.println("Takeaway: cross-package protected = subclass relationship + own-type reference. Both required.");
    }
}
