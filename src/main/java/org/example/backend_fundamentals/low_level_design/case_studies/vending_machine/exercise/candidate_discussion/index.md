---
search: false
---

# Candidate discussion — Vending Machine

Start by proposing a bounded first scope:

> “I’ll model one in-memory machine that sells one item per cash transaction. It will track item stock,
> accept supported denominations, return exact change from its cash inventory, and refund the customer if
> it cannot complete the purchase.”

Then confirm the decisions that change the model or purchase flow.

## Questions to ask

1. Is this one physical machine with a single active customer transaction, or must it serve customers
   concurrently?
2. Is the machine layout fixed, or should slots be configured by stable slot code rather than row/column position?
3. Are products identified by slot code, and does each slot contain one product type with a price and
   quantity?
4. Is payment cash only for the first version? Which denominations can the machine accept and dispense?
5. May a customer select only one item per transaction? Can they change selection after inserting cash?
6. Must the machine return physical denominations as change? What happens when it cannot return exact
   change?
7. What should cancellation, insufficient payment, sold-out stock, and an invalid slot code do?
8. Are restocking, card payments, persistence, telemetry, and a UI part of this interview round?

## Agreed scope for this exercise

- One in-memory machine serves one cash transaction at a time.
- Layout is represented by configurable, stable slot codes; rows and columns are display concerns, not the domain key.
- Each slot has a unique code, one product, a unit price in integer minor units, and an available quantity.
- A customer selects one in-stock slot and inserts supported cash denominations.
- Selection is fixed once cash has been inserted. Changing the item requires cancelling the current transaction and
  starting another.
- The machine vends exactly one product when payment is sufficient and it can return exact change from its
  cash inventory.
- If payment is insufficient, the slot is invalid or sold out, the customer cancels, or exact change is
  unavailable, do not reduce product inventory; return the inserted amount where applicable and clear the
  active transaction.
- Restocking, card payments, multiple simultaneous transactions, persistence, and reporting are follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
