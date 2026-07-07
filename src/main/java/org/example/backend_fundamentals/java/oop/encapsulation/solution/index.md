---
order: 20
search: false
---

# Solutions

## Solution: data-hiding - Protect internal state

```java
public class BankAccount {
    // Hidden internal state
    private double balance;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }

    // Controlled access
    public void deposit(double amount) {
        this.balance += amount;
    }
    
    public double getBalance() {
        return this.balance;
    }
}
```

## Solution: invariants - Enforce invariants

```java
public class BankAccount {
    private double balance;

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (this.balance - amount < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance -= amount;
    }
}
```
