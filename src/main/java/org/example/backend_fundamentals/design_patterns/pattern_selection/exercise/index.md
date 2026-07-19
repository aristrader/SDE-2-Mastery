---
order: 10
search: false
---

# Pattern Selection Exercises

## Exercise: checkout-design-review - Review a Flawed Checkout Design

Review this like a production pull request. Ignore syntax polish and naming nits. Focus on responsibilities, dependency direction, runtime selection, and whether each abstraction is earning its place.

```java
public interface DiscountService {
    double apply(Order order);
}

public class DiscountServiceImpl implements DiscountService {
    @Override
    public double apply(Order order) {
        if (order.isPremiumCustomer()) {
            return order.getSubtotal() * 0.20;
        }
        if (order.isEmployee()) {
            return order.getSubtotal() * 0.30;
        }
        return 0;
    }
}

public interface PaymentProcessor {
    void pay(Order order);
}

public class CardPaymentProcessor implements PaymentProcessor {
    @Override
    public void pay(Order order) {
        // charge card
    }
}

public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public void pay(Order order) {
        // charge UPI
    }
}

public class Order {
    private String status;
    private List<OrderItem> items;

    public double getSubtotal() {
        return items.stream().mapToDouble(OrderItem::price).sum();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void releaseInventory() {
        // call inventory system
    }

    public void sendCancellationEmail() {
        // call email system
    }
}

public class CheckoutService {
    private final DiscountService discountService;
    private final CardPaymentProcessor cardPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;

    public CheckoutService(
            DiscountService discountService,
            CardPaymentProcessor cardPaymentProcessor,
            UpiPaymentProcessor upiPaymentProcessor) {
        this.discountService = discountService;
        this.cardPaymentProcessor = cardPaymentProcessor;
        this.upiPaymentProcessor = upiPaymentProcessor;
    }

    public void checkout(Order order, String paymentType) {
        double discount = discountService.apply(order);
        if ("CARD".equals(paymentType)) {
            cardPaymentProcessor.pay(order);
        } else if ("UPI".equals(paymentType)) {
            upiPaymentProcessor.pay(order);
        }
        order.setStatus("PAID");
    }
}

public final class ShippingUtil {
    private ShippingUtil() {
    }

    public static double calculateShipping(Order order) {
        return order.getSubtotal() > 1000 ? 0 : 99;
    }
}
```

Answer these:

1. Which classes have the wrong dependency direction?
2. Which abstractions are useful, and which are only ceremony?
3. Where should runtime payment selection live?
4. Which `Order` methods belong on the entity, and which should move to services?
5. Is `ShippingUtil` really a utility, or is it a business policy?
6. What refactor would you make first if you had to improve this in one small PR?

## Exercise: interface-or-class - Decide Whether the Interface Earns Its Place

For each case, decide whether to keep an interface, use a concrete class directly, or use an abstract class.

1. `UserService` has exactly one `UserServiceImpl`. No tests mock it and no other module consumes a separate contract.
2. `PaymentProcessor` has card, UPI, wallet, and bank-transfer implementations selected from request data.
3. `VerificationFlow` has one fixed workflow but different document-specific extraction steps. The workflow order must not be overridden.
4. `ShippingCostCalculator` has one implementation today, but pricing, carrier, and region rules are explicitly owned by a separate business team and expected to vary.

## Exercise: strategy-or-template - Pick the Better Pattern

Pick Strategy, Template Method, Registry/Resolver, or plain code.

1. A checkout service charges card, UPI, or wallet based on request input.
2. A report generator always loads data, validates it, formats it, and uploads it, but CSV and PDF customize formatting.
3. A service currently has one four-line helper that calculates a flat fee.
4. A Spring app has ten `FraudRule` beans and must run only the ones configured for a tenant.

## Trick questions / gotchas

**Q. If `CheckoutService` depends on `PaymentProcessor`, is runtime selection automatically solved?**
A. No. DIP fixes the dependency type. A resolver or registry handles "which implementation for this request?"

**Q. Should every service have an interface in Spring?**
A. No. Constructor injection works with concrete classes too. Add the interface only when there is a real substitution, boundary, or selection need.

**Q. Does a four-line method mean "utility"?**
A. No. Business meaning, ownership, and likely change pattern matter more than line count.

## Quick recall

**Q. First question in a design review?**
A. "What pain or responsibility split is this code hiding?"

**Q. Payment processor selection belongs where?**
A. In a resolver/registry or DI-wired map, not in checkout orchestration.

**Q. Entity responsibility rule?**
A. State-only decisions can live on the entity; external coordination belongs in services.
