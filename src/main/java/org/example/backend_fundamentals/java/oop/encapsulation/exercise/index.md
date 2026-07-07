---
order: 10
search: false
---

# Practice

## Exercise: data-hiding - Protect internal state

### Goal
Hide internal state from outside interference using access modifiers.

### Task
Create a `BankAccount` class with a `public double balance`. In a `main` method, show how external code can directly set the balance to `-1000`.
Then, encapsulate the `balance` field (make it private) and provide a `public void deposit(double amount)` method.

### Checks
- Can external code directly modify the balance anymore?

## Exercise: invariants - Enforce invariants

### Goal
Use encapsulation to protect internal consistency (invariants).

### Task
Update your `deposit` method to throw an `IllegalArgumentException` if the amount is negative.
Add a `withdraw` method that throws an exception if the withdrawal would result in a negative balance.

### Checks
- Try to deposit `-50` and catch the exception.
- Try to withdraw more than the balance and catch the exception.
