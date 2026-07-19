---
order: 20
search: false
---

# Pattern Selection Solutions

## Solution: checkout-design-review - Review a Flawed Checkout Design

The main problems are not syntax problems. They are ownership and dependency problems.

`CheckoutService` violates DIP because it depends on `CardPaymentProcessor` and `UpiPaymentProcessor` directly while also depending on the `PaymentProcessor` idea implicitly. It should depend on a `PaymentProcessorResolver` or a `Map<PaymentType, PaymentProcessor>`.

```java
public final class CheckoutService {
    private final DiscountService discountService;
    private final PaymentProcessorResolver paymentProcessorResolver;

    public CheckoutService(
            DiscountService discountService,
            PaymentProcessorResolver paymentProcessorResolver) {
        this.discountService = discountService;
        this.paymentProcessorResolver = paymentProcessorResolver;
    }

    public void checkout(Order order, PaymentType paymentType) {
        double discount = discountService.apply(order);
        order.applyDiscount(discount);
        paymentProcessorResolver.forType(paymentType).pay(order);
        order.markPaid();
    }
}
```

The resolver owns runtime selection:

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

`DiscountService` is not automatically wrong, but `DiscountServiceImpl` is carrying multiple independently changing business policies in one `if/else` chain. If premium, employee, coupon, campaign, and region discounts change independently, split them into discount rules or strategies. If those rules are tiny and stable today, one small service can be acceptable until the variation is real.

`Order.getSubtotal()` belongs on `Order` because it depends only on order state. `setStatus(String)` is weak because callers can put the order in any state with any string. Prefer meaningful transitions such as `markPaid()` and `cancel()`.

`Order.releaseInventory()` and `Order.sendCancellationEmail()` should move out. They coordinate external systems. The order can decide that it is cancelled; an application service should coordinate inventory and email.

`ShippingUtil` is suspicious. If it is pure mechanical formatting, a utility is fine. If it represents shipping policy, make the concept explicit:

```java
public interface ShippingCostCalculator {
    Money calculate(Order order);
}
```

The first small PR should usually fix payment selection because it has the clearest DIP violation and the highest extension pressure. Add `PaymentProcessorResolver`, change `CheckoutService` to depend on it, and leave discount/shipping cleanup for follow-up PRs unless they are already causing changes.

## Solution: interface-or-class - Decide Whether the Interface Earns Its Place

1. Use the concrete `UserService` class directly. A one-to-one `UserService` / `UserServiceImpl` pair is ceremony unless a boundary, fake, or second implementation exists.
2. Keep `PaymentProcessor` as an interface. Multiple implementations exist and runtime selection is real.
3. Use an abstract class. Template Method needs a fixed workflow, protected hooks, and usually a `final` workflow method.
4. Either is defensible. If variation is genuinely expected and owned by a separate business policy area, naming `ShippingCostCalculator` now can be useful. If that expectation is vague, start concrete and extract the interface when the second implementation appears.

## Solution: strategy-or-template - Pick the Better Pattern

1. Strategy plus resolver. The request chooses card, UPI, or wallet at runtime.
2. Template Method if the load-validate-format-upload workflow must be fixed and only formatting varies.
3. Plain code. A single four-line helper does not earn a pattern by itself.
4. Registry/Resolver, usually DI-wired in Spring. The tenant config chooses which `FraudRule` implementations run.

## Quick recall

**Q. Why not inject `CardPaymentProcessor` and `UpiPaymentProcessor` directly?**
A. Checkout now knows every payment type. Adding a payment method edits checkout, which is exactly what DIP/OCP are trying to avoid.

**Q. Why is `setStatus(String)` weak?**
A. It exposes raw state mutation and allows invalid transitions. Domain methods like `markPaid()` preserve intent and invariants.

**Q. Is a resolver the same as Strategy?**
A. No. Strategy is the behavior object. Resolver is the object that chooses the strategy.
