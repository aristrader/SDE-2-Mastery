package org.example.backend_fundamentals.java.oop.access_modifiers;

/** Concrete subclass — fills in the hook; the template handles everything else. */
public class ReportProcess extends ProcessTemplate {

    private final String reportName;

    public ReportProcess(String reportName) {
        super();   // protected constructor — allowed because we are subclassing
        this.reportName = reportName;
    }

    @Override
    protected void doWork() {
        System.out.println("  [doWork]   generating report: " + reportName);
    }
}
