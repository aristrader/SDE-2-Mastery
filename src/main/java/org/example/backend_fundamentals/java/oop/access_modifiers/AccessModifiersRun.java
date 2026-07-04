package org.example.backend_fundamentals.java.oop.access_modifiers;

/**
 * Demo runner for access modifier concepts.
 *
 * <p>Three demos:</p>
 * <ol>
 *   <li>Four visibility levels — {@link BankAccount} + {@link BankLedger}</li>
 *   <li>protected constructor / public final / protected abstract / private helpers — {@link ProcessTemplate}</li>
 *   <li>static nested vs non-static inner class — {@link Container}</li>
 * </ol>
 */
public class AccessModifiersRun {

    public static void main(String[] args) {

        // ── Demo 1: four visibility levels ────────────────────────────────────────
        System.out.println("=== 1. Four visibility levels (BankAccount + BankLedger) ===");

        BankAccount account = new BankAccount(1234, 1000.0);
        account.deposit(500.0);
        System.out.println("Balance (correct pin 1234): " + account.getBalance(1234));
        System.out.println("Balance (wrong pin 9999):   " + account.getBalance(9999));

        // applyInterest() is package-private — callable from here (same package), not from outside
        BankLedger ledger = new BankLedger();
        ledger.register(account);
        ledger.runMonthEnd();
        System.out.println("Balance after month-end:    " + account.getBalance(1234));

        // ── Demo 2: protected constructor + public final + protected abstract ─────
        System.out.println();
        System.out.println("=== 2. ProcessTemplate — protected ctor, public final, protected abstract ===");

        ProcessTemplate process = new ReportProcess("Q1 Summary");
        process.execute();
        process.doWork();  // accessible: same package as ProcessTemplate (protected = same-package OR subclass)
        // process.validate() — compile error: private is invisible to everyone outside the class itself

        // ── Demo 3: static nested vs inner class ──────────────────────────────────
        System.out.println();
        System.out.println("=== 3. static nested (Config) vs inner class (Cursor) ===");

        // Config is static: no Container instance needed
        Container.Config config = new Container.Config(10);
        System.out.println("Config created without Container. pageSize=" + config.pageSize);

        // Cursor is non-static: requires a Container instance; holds a hidden reference to it
        Container container = new Container("alpha", "beta", "gamma");
        Container.Cursor cursor = container.new Cursor();
        System.out.print("Cursor reads: ");
        while (cursor.hasNext()) System.out.print(cursor.next() + " ");
        System.out.println();
        System.out.println("(cursor holds Container.this invisibly — container cannot be GC'd while cursor is alive)");
    }
}
