package org.example.backend_fundamentals.java.foundations.access_modifiers.other;

import org.example.backend_fundamentals.java.foundations.access_modifiers.ProcessTemplate;
import org.example.backend_fundamentals.java.foundations.access_modifiers.ReportProcess;

/**
 * SCENARIOS B and C — subclass in a different package.
 *
 * <p>This class extends {@link ProcessTemplate} from a different package. So the FIRST
 * condition for cross-package {@code protected} access is satisfied: this class IS a subclass.</p>
 *
 * <p>But there's a <strong>second condition</strong> that surprises most Java programmers:
 * the compiler also requires that the access happen through a reference whose <em>declared type</em>
 * is this class itself ({@code OtherSubclass}) or a subtype of it.</p>
 *
 * <p>Concretely:</p>
 * <ul>
 *   <li>{@code this.doWork()} — works. Type of {@code this} is {@code OtherSubclass}.</li>
 *   <li>{@code super.doWork()} — works. Calls the inherited member directly.</li>
 *   <li>{@code OtherSubclass o = new OtherSubclass(); o.doWork();} — works. Reference type matches.</li>
 *   <li>{@code ProcessTemplate p = ...; p.doWork();} — <strong>compile error</strong>. Reference type
 *       is the parent class, NOT {@code OtherSubclass} or a subtype of it.</li>
 *   <li>{@code ReportProcess r = ...; r.doWork();} — <strong>compile error</strong>. {@code ReportProcess}
 *       is a sibling subclass, not {@code OtherSubclass} or a subtype of it.</li>
 * </ul>
 *
 * <p><strong>Why this rule exists:</strong> Java is protecting siblings from each other. Every subclass
 * gets its own inherited {@code protected} member. The rule ensures that {@code OtherSubclass} can use
 * <em>its own copy</em> of {@code doWork()}, but cannot reach into a {@code ReportProcess} or any other
 * sibling-class instance through a parent reference and invoke its {@code protected} internals.
 * Without this rule, sibling subclasses could break each other's encapsulation.</p>
 */
public class OtherSubclass extends ProcessTemplate {

    @Override
    protected void doWork() {
        System.out.println("    [OtherSubclass.doWork] running in different package");
    }

    /** SCENARIO C — accessing protected through own-type references. All allowed. */
    public void scenarioC_ownTypeAccess() {
        System.out.println("--- Scenario C: subclass in different package, own-type access ---");

        this.doWork();                   // ✅ 'this' is typed OtherSubclass — same as accessing class
        super.execute();                 // ✅ public method — irrelevant to protected rules

        OtherSubclass self = new OtherSubclass();
        self.doWork();                   // ✅ reference type is OtherSubclass — same as accessing class

        // super.doWork() also works — direct access to the inherited member.
        // Skipped here only to keep the demo output tidy.
    }

    /** SCENARIO B — accessing protected through the wrong variable type. Rejected. */
    public void scenarioB_siblingThroughParentType() {
        System.out.println("--- Scenario B: subclass, but accessing through wrong reference type ---");

        // Java's rule for cross-package protected access:
        //   The LABEL on the variable (the type on the left of '=') must be either
        //   your own class name (OtherSubclass), or a class that EXTENDS your class.
        //   Java does not look at the actual object inside the variable. Only the label.

        // ── First attempt: label says ProcessTemplate (the parent) ────────────────
        ProcessTemplate sibling = new ReportProcess("A different subclass instance");
        sibling.execute();        // ✅ public — public stuff is open to everyone

        // sibling.doWork();      // ❌ Compile error.
        //   Variable label:  ProcessTemplate
        //   My class (caller): OtherSubclass
        //   Java asks: "Is ProcessTemplate the same as OtherSubclass, or does it extend OtherSubclass?"
        //   No. ProcessTemplate is the PARENT — it sits ABOVE OtherSubclass in the family tree.
        //   Wrong direction. Rejected.

        // ── Second attempt: label says ReportProcess (a cousin) ───────────────────
        ReportProcess sibling2 = new ReportProcess("Sibling, typed concretely");
        sibling2.execute();       // ✅ public — fine

        // sibling2.doWork();     // ❌ Compile error.
        //   Variable label:  ReportProcess
        //   My class (caller): OtherSubclass
        //   Java asks: "Is ReportProcess the same as OtherSubclass, or does it extend OtherSubclass?"
        //   No. ReportProcess extends ProcessTemplate, and OtherSubclass also extends ProcessTemplate.
        //   They're COUSINS — they share the same parent, but neither extends the other.
        //   Cousins cannot reach into each other's protected stuff. Rejected.
        //
        //   Family tree:
        //                       ProcessTemplate
        //                       /             \
        //                ReportProcess    OtherSubclass  ← me, the caller
        //                (cousin)

        System.out.println("    (the doWork() calls above are commented out — they would not compile)");
    }
}
