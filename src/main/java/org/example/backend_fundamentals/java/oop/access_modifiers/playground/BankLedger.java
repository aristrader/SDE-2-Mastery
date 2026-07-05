package org.example.backend_fundamentals.java.oop.access_modifiers.playground;

import java.util.ArrayList;
import java.util.List;

/**
 * Same-package class that accesses {@link BankAccount}'s package-private members directly.
 *
 * <p>This is why {@code interestRate} and {@code applyInterest()} are package-private rather than
 * private — they're an internal concern shared between sibling classes in the same package,
 * not part of the public API.</p>
 */
public class BankLedger {

    private final List<BankAccount> accounts = new ArrayList<>();

    public void register(BankAccount account) {
        accounts.add(account);
    }

    /** Applies interest to every account — uses package-private access on each. */
    public void runMonthEnd() {
        for (BankAccount account : accounts) {
            System.out.printf("  Ledger: applying %.0f%% interest%n", account.interestRate * 100);
            account.applyInterest();   // package-private method — accessible here, not from outside
        }
    }
}
