package org.example.backend_fundamentals.java.oop.pillars.encapsulation.playground.other;

import org.example.backend_fundamentals.java.oop.pillars.encapsulation.playground.PackageLevel;

/**
 * The cross-package side of package-level encapsulation.
 * <p>
 * This class lives in a <em>different</em> package than {@link PackageLevel}
 * (the {@code .other} sub-package). It demonstrates two things side by side:
 * <ul>
 *   <li><strong>What works:</strong> {@code public} members ({@code banner},
 *       {@code shout()}) are reachable from outside the package.</li>
 *   <li><strong>What does not work:</strong> package-private members ({@code note},
 *       {@code greet()}) are invisible from outside, and the package-private peer
 *       class {@code SamePackagePeer} doesn't even exist as a name out here.</li>
 * </ul>
 * <p>
 * The lines marked "would not compile" are kept as comments so the file still
 * builds; uncomment any of them to watch javac reject it.
 */
public class CrossPackageAccessRun {

  public static void main(String[] args) {
    PackageLevel pl = new PackageLevel();
    System.out.println("=== Public members — these WORK from outside the package ===");
    System.out.println("Read public field:    " + pl.banner);
    pl.shout();

    System.out.println();
    System.out.println("=== Package-private members — these would NOT compile from here ===");

    // System.out.println(pl.note);
    //   error: note has package access in
    //   org.example...encapsulation.PackageLevel

    // pl.greet();
    //   error: greet() has package access in
    //   org.example...encapsulation.PackageLevel

    // SamePackagePeer peer = new SamePackagePeer();
    //   error: SamePackagePeer is not public in
    //   org.example...encapsulation; cannot be accessed from outside package
    //   (the import itself wouldn't even resolve)

    System.out.println("Lesson: across a package boundary, public is reachable, package-private is not.");
    System.out.println("The class itself is public — that's all you getValue; its package-private state stays hidden.");
  }
}
