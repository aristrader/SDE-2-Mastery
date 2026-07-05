package org.example.backend_fundamentals.java.oop.encapsulation.playground;

/**
 * Encapsulation enforced by package boundary.
 * <p>
 * This class deliberately has both {@code public} and package-private members so the
 * cross-package demo can show the contrast in one place:
 * <ul>
 *   <li>{@link #banner} and {@link #shout()} are {@code public} — visible everywhere.</li>
 *   <li>{@link #note} and {@link #greet()} have no modifier, i.e. <em>package-private</em>
 *       — visible only inside this package.</li>
 * </ul>
 * <p>
 * A peer class in this package ({@link SamePackagePeer}) can touch all four. A class
 * in a different package can touch the {@code public} two but not the package-private
 * two — see {@code other/CrossPackageAccessRun.java}.
 */
public class PackageLevel {

  // public — visible to anyone who can see the class.
  public String banner = "public field";

  // No modifier = package-private. Same-package code can touch it; outsiders cannot.
  String note = "package-private field";

  // public method — callable from anywhere.
  public void shout() {
    System.out.println("hello from a PUBLIC method");
  }

  // Package-private method.
  void greet() {
    System.out.println("hello from a package-private method");
  }

  public static void main(String[] args) {
    PackageLevel target = new PackageLevel();
    SamePackagePeer peer = new SamePackagePeer();
    peer.use(target);
  }
}

/**
 * A peer class in the same package. Allowed to touch every member of
 * {@link PackageLevel}, public or package-private, because they share the same package.
 */
class SamePackagePeer {

  void use(PackageLevel pl) {
    System.out.println("Peer read public field:    " + pl.banner);
    System.out.println("Peer read package field:   " + pl.note);
    pl.shout();
    pl.greet();
    // From a class in a DIFFERENT package, the two package-private accesses
    // (pl.note and pl.greet()) would fail to compile; the public ones work.
  }
}
