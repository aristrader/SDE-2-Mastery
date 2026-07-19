---
order: 10
---

# Strategy vs Template Method

Use this comparison when the code has multiple ways to perform a task and you are deciding whether to pass behavior in, select it from a registry, or lock a workflow in a parent class.

## How they work

**Strategy** means the caller or a resolver supplies a behavior object. The context delegates the varying algorithm to that object.

```java
public interface PaymentProcessor {
    void pay(Order order);
}

public final class CheckoutService {
    private final PaymentProcessorResolver resolver;

    public CheckoutService(PaymentProcessorResolver resolver) {
        this.resolver = resolver;
    }

    public void checkout(Order order, PaymentType type) {
        resolver.forType(type).pay(order);
        order.markPaid();
    }
}
```

`CardPaymentProcessor` and `UpiPaymentProcessor` each own a complete payment algorithm. `CheckoutService` owns the checkout flow and delegates only the payment behavior.

**Template Method** means a parent class owns the fixed workflow and subclasses override selected steps.

```java
public abstract class VerificationFlow {
    public final VerificationResult verify(Document document) {
        validateInput(document);
        ExtractedData data = extract(document);
        return decide(data);
    }

    protected abstract ExtractedData extract(Document document);
    protected abstract VerificationResult decide(ExtractedData data);

    private void validateInput(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document is required");
        }
    }
}
```

The parent owns the lifecycle. Subclasses fill hooks, but they cannot replace the whole `verify()` workflow.

## Decision rule

| Question | Choose |
| --- | --- |
| Does each implementation own a completely different algorithm? | Strategy |
| Does the caller or request data choose the behavior at runtime? | Strategy + resolver or registry |
| Is there one fixed workflow with a few variable steps? | Template Method |
| Must subclasses be prevented from replacing the workflow? | Template Method with a `final` method |
| Do you need shared state, constructor-injected collaborators, or protected hooks? | Abstract class / Template Method |

The short version: Strategy asks "which behavior should I delegate to?" Template Method asks "which steps may subclasses customize inside my workflow?"

## Resolver is a separate responsibility

Do not confuse "depend on an interface" with "runtime selection is solved."

```java
public final class PaymentProcessorResolver {
    private final Map<PaymentType, PaymentProcessor> processors;

    public PaymentProcessorResolver(Map<PaymentType, PaymentProcessor> processors) {
        this.processors = Map.copyOf(processors);
    }

    public PaymentProcessor forType(PaymentType type) {
        PaymentProcessor processor = processors.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
        return processor;
    }
}
```

`CheckoutService` should not contain `if (type == CARD) new CardPaymentProcessor(...)`. DIP removes concrete type coupling; the resolver removes selection coupling.

## Gotchas / pitfalls

- Do not create an interface for every service by habit. If there is exactly one implementation and no testing, module-boundary, or runtime-selection pressure, the interface is ceremony.
- Do not use Strategy just because there are multiple types. Use it when behavior varies independently and the context should not own those branches.
- Do not implement Template Method with an interface default method when the workflow must be locked. Interface default methods cannot be `final`.
- Do not put external coordination on an entity just because the action is "about" that entity. An `Order` can mark itself cancelled; inventory release and cancellation email belong to services.
- Do not turn every short method into a utility. A short method can still represent a business policy that deserves a named class.

## Quick recall

**Q. Strategy vs Template Method in one line?**
A. Strategy delegates a whole behavior object; Template Method locks a parent workflow and lets children override selected steps.

**Q. Why does Template Method usually use an abstract class?**
A. It can hold state, enforce construction, expose protected hooks, and mark the workflow `final`.

**Q. What does a resolver add to Strategy?**
A. It centralizes runtime selection, so the service depends on behavior but does not know how to choose implementations.

**Q. When is an interface not earning its place?**
A. When deleting it only removes `implements` and changes no design pressure: no second implementation, no boundary, no runtime selection, no mocking need.

**Q. Payment example: Strategy or Template Method?**
A. Usually Strategy. Card and UPI have different complete payment algorithms; checkout delegates payment to the selected processor.

**Q. Verification pipeline with fixed validate-extract-decide order?**
A. Template Method if the parent must enforce that order and subclasses only customize extraction/decision.
