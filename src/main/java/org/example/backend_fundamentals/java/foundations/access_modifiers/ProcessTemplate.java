package org.example.backend_fundamentals.java.foundations.access_modifiers;

/**
 * Abstract class that shows the same modifier choices as {@code DeveloperHiringProcess}
 * from the factory_method package — explained here in isolation.
 *
 * <ul>
 *   <li>{@code protected} constructor — subclasses can call super(), but nobody can call
 *       {@code new ProcessTemplate()} directly. Enforces "always subclass me."</li>
 *   <li>{@code public final} execute() — the template method. Public so any caller can
 *       trigger the workflow; final so no subclass can rewire the steps.</li>
 *   <li>{@code protected abstract} doWork() — the hook. Protected because it is an internal
 *       concern between this class and its subclasses; abstract because there is no
 *       sensible default implementation.</li>
 *   <li>{@code private} validate() / cleanup() — steps owned entirely by this class;
 *       invisible to and unoverrideable by subclasses.</li>
 * </ul>
 */
public abstract class ProcessTemplate {

    protected ProcessTemplate() { }    // protected: must subclass; random callers cannot instantiate

    public final void execute() {      // public final: template — callable by all, overrideable by none
        validate();
        doWork();
        cleanup();
    }

    protected abstract void doWork();  // protected abstract: subclass fills this in

    private void validate() {
        System.out.println("  [validate] running pre-checks");
    }

    private void cleanup() {
        System.out.println("  [cleanup]  releasing resources");
    }
}
