---
order: 20
search: false
---

# Solutions

## Solution: abstract-factory - Abstract Factory variation
Keep delivery on the runner when it is consumption/orchestration outside the factory's responsibility; that remains more natural for the current domain. Use an abstract base only if every factory owns the same locked delivery algorithm or shared base state. A stateless shared convenience could instead be an interface default method, provided implementations are allowed to override it.

## Solution: strategy-flavours - Strategy pattern in two flavours
```java
interface PaymentStrategy {
    void pay(int amount);
}

class CardPayment implements PaymentStrategy {
    public void pay(int amount) { System.out.println("card " + amount); }
}

abstract class AuditedPaymentStrategy {
    public final void pay(int amount) {
        audit(amount);
        doPay(amount);
    }

    protected abstract void doPay(int amount);
    private void audit(int amount) { System.out.println("audit " + amount); }
}

class CashPayment extends AuditedPaymentStrategy {
    protected void doPay(int amount) { System.out.println("cash " + amount); }
}

class StrategyDemo {
    public static void main(String[] args) {
        new CardPayment().pay(10);
        new CashPayment().pay(10);
    }
}
```

The interface version is right when payment is only a substitutable role. The abstract-base version is justified only when every strategy must use the same audit algorithm and hooks; otherwise it unnecessarily consumes the single superclass slot.

## Solution: default-methods - Default methods test
A `default` method works for convenience behavior based only on other interface methods (for example, `default void runTwice() { run(); run(); }`). Promote to an abstract base when the shared behavior needs instance state, constructor invariants, protected hooks, or a final template algorithm.

```java
interface Left {
    default String label() { return "left"; }
}

interface Right {
    default String label() { return "right"; }
}

class Both implements Left, Right {
    @Override
    public String label() {
        return Left.super.label(); // explicit conflict resolution
    }
}
```

Two unrelated defaults require `Both` to override. A class method wins over a default, and a more-specific subinterface default wins over a parent-interface default.

## Solution: builder-hierarchy - Builder hierarchy (EJ Item 2)
`Pizza` is an abstract class because it holds state (the `Set<Topping> toppings` field) and provides the actual mutation methods for that state. An interface cannot hold this state, forcing every subclass to duplicate the topping collection field and its mutators.
