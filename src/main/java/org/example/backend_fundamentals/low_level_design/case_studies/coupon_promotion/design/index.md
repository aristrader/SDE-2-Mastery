---
order: 20
search: false
---

# Design notes — Coupon / Promotion Rules

Use this page after attempting the exercise. The difficult part is not subtracting a number: it is making
eligibility, discount calculation, and redemption state agree without consuming a coupon on a failed request.

## Start from one checkout

A caller supplies a cart and one coupon code. The service first finds the coupon, then proves it can apply,
calculates the discount on the eligible portion of the cart, and only then records one redemption.

```mermaid
sequenceDiagram
    participant Caller
    participant Service as Coupon service
    participant Coupon
    participant Cart
    participant Discount

    Caller->>Service: apply(cart, couponCode)
    Service->>Coupon: exists, active, usage remaining?
    Service->>Cart: calculate cart and eligible subtotals
    alt invalid or ineligible
        Service-->>Caller: reject; no state changed
    else eligible
        Service->>Discount: calculate(eligibleSubtotal)
        Service->>Coupon: increment redemption count
        Service-->>Caller: discount and final total
    end
```

The reader question this flow answers is: **when is it safe to consume a coupon?** Only after every
validation passes and the final discount is known.

## Responsibilities and invariant

| Responsibility | Own it here | Why |
| --- | --- | --- |
| Cart subtotal and eligible subtotal | `Cart` / line items | It knows prices, quantities, and product IDs. |
| Coupon availability | `Coupon` | Its dates and global redemption count define whether it is still usable. |
| Request orchestration | `CouponService` | It coordinates lookup, validation, calculation, and the state mutation. |
| Discount arithmetic | `Discount` implementation | Fixed and percentage rules vary independently. |

The key invariant is: **a rejected request changes neither the cart result nor the coupon redemption count.**
For a valid request, the final total is never negative and the redemption count rises exactly once.

## Derive eligibility before calculation

The base coupon is usable only when all of these are true:

1. Its code exists.
2. The current time is inside its start/end window.
3. Its redemption count is below its global limit.
4. The complete cart subtotal meets its optional minimum subtotal.
5. It has a positive eligible subtotal when product restrictions exist.

If the coupon has no product restriction, every line item contributes to the eligible subtotal. If it targets
specific product IDs, only those items contribute. That distinction matters: a `20%` coupon restricted to one
product must not discount unrelated items in the same cart.

## Keep discount types small

The service gives a discount rule an already-computed eligible subtotal. The rule should not look up coupons,
change usage count, or inspect cart configuration.

| Type | Calculation | Required cap |
| --- | --- | --- |
| Fixed amount | configured minor-unit amount | at most eligible subtotal |
| Percentage | `eligibleSubtotal × percentage / 100` | at most eligible subtotal |

Using integer minor units avoids price precision errors. Integer division also gives a deterministic rounding
rule for the exercise; state it explicitly if an interviewer asks.

## Deliberate scope boundary

This design supports one coupon only. Stacking is not “just loop over coupons”: the product must define an
ordering rule, whether one rule sees another rule's discounted total, and which combinations are incompatible.
Keep those decisions out of the base solution.

For concurrent or persisted redemption, the check and increment must become one atomic operation. Otherwise
two checkouts can both observe the final available redemption and oversubscribe the coupon.

## Quick recall

- What changes coupon state? A successful, fully validated application only.
- Why calculate an eligible subtotal first? Product restrictions must not discount unrelated items.
- Why not add stacking now? Ordering and compatibility are product rules, not an implementation detail.
