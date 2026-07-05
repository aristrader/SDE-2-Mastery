package org.example.backend_fundamentals.java.oop.access_modifiers.playground.other;

import org.example.backend_fundamentals.java.oop.access_modifiers.playground.Container;
import org.example.backend_fundamentals.java.oop.access_modifiers.playground.ProcessTemplate;
import org.example.backend_fundamentals.java.oop.access_modifiers.playground.ReportProcess;

/**
 * SCENARIO A — unrelated class in a different package.
 *
 * <p>This class lives in {@code .access_modifiers.other} (a different package from
 * {@link ProcessTemplate}) AND it does <strong>not extend</strong> {@code ProcessTemplate}.
 * It has no inheritance claim on the parent's {@code protected} members.</p>
 *
 * <p>Result: only {@code public} members of {@code ProcessTemplate} are reachable here.
 * The {@code protected doWork()} method is invisible — even when staring at an actual
 * {@link ReportProcess} instance, the compiler says no.</p>
 */
public class OtherPackageRunner {

    public void demo() {
        ProcessTemplate p = new ReportProcess("From other package — unrelated runner");

        p.execute();      // ✅ public — fine, fires the whole template (validate + doWork + cleanup)

        // p.doWork();    // ❌ Compile error.
        //   Rule: cross-package access to a protected member requires that the accessing class
        //   (OtherPackageRunner) be a subclass of the declaring class (ProcessTemplate).
        //   It isn't. So no protected access — even though p actually points to a ReportProcess
        //   at runtime. The compiler doesn't care about the runtime type; it checks against
        //   the declared type and the calling class's relationship to it.

        System.out.println("  OtherPackageRunner could only call execute() — protected doWork() is invisible.");
    }
}
