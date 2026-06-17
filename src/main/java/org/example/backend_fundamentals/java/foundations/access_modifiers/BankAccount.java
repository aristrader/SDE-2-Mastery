package org.example.backend_fundamentals.java.foundations.access_modifiers;

/**
 * Demonstrates all four visibility levels on a single class.
 *
 * <ul>
 *   <li>{@code private}        — balance, pin: owned exclusively by this class; no external read/write.</li>
 *   <li>package-private        — interestRate: {@link BankLedger} (same package) reads it directly;
 *       outside callers go through the public API.</li>
 *   <li>{@code public}         — deposit, getBalance: the client-facing contract.</li>
 * </ul>
 *
 * <p>No {@code protected} fields here — protected is for inheritance hierarchies, not for
 * data a sibling class in the same package needs. Use package-private for that.</p>
 */
public class BankAccount {

    private double balance;         // private: only this class manages the balance
    private final int pin;          // private + final: security chokepoint, set once at construction

    double interestRate = 0.05;     // package-private: BankLedger reads this directly

    public BankAccount(int pin, double initialBalance) {
        this.pin = pin;
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    /** Returns the balance if the pin matches, -1 otherwise. */
    public double getBalance(int enteredPin) {
        return validatePin(enteredPin) ? balance : -1;
    }

    /** Package-private: only called by {@link BankLedger} in the same package. */
    void applyInterest() {
        balance += balance * interestRate;
    }

    private boolean validatePin(int entered) {  // private: internal implementation detail
        return entered == pin;
    }
}
