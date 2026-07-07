---
order: 20
search: false
---

# Exception Handling Solutions

## Solution: checked-vs-unchecked - Extend the hierarchy
```java
// InsufficientFundsException represents an expected business failure condition that callers must handle.
// InvalidAccountStateException represents a programming error (bad argument).
class InsufficientFundsException extends Exception {
    private final double shortfall;
    public InsufficientFundsException(String msg, double shortfall) { super(msg); this.shortfall = shortfall; }
    public double getShortfall() { return shortfall; }
}

class InvalidAccountStateException extends RuntimeException {
    public InvalidAccountStateException(String msg) { super(msg); }
}

void withdraw(double balance, double amount) throws InsufficientFundsException {
    if (amount <= 0) throw new InvalidAccountStateException("Amount must be positive");
    if (amount > balance) throw new InsufficientFundsException("Not enough funds", amount - balance);
    System.out.println("Withdrew " + amount);
}
```

## Solution: exception-translation - Exception translation
```java
class ConfigLoadException extends RuntimeException {
    public ConfigLoadException(String msg, Throwable cause) { super(msg, cause); }
}

void loadUserConfig(String filename) {
    try {
        readFile(filename);
    } catch (IOException e) {
        throw new ConfigLoadException("Failed to load config", e);
    }
}
```

## Solution: try-with-resources-warmup - Warm-up (close ordering)
```java
try (Resource r1 = new Resource("r1"); Resource r2 = new Resource("r2")) {
    r1.use(); r2.use();
}
// r2 closes first, then r1. Resources are closed in reverse order of initialization.
```

## Solution: try-with-resources-exception - Close with exception
```java
try (Resource r1 = new Resource("r1")) {
    r1.use();
    throw new RuntimeException("Body failed");
} catch (Exception e) {
    System.out.println(e.getMessage()); // Body failed
    System.out.println(e.getSuppressed()[0].getMessage()); // Close failed: r1
}
// The body exception is primary because it represents the root cause of the failure. 
// The close exception is secondary/suppressed because it's a side-effect of the cleanup process.
```
