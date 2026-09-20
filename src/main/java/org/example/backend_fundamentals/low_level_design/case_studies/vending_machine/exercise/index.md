---
order: 10
search: false
---

# Final exercise — Vending Machine LLD

## Exercise: vending-machine-lld - Cash Purchase Flow

### Goal

Model the smallest useful in-memory vending machine for one-item cash purchases.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Constraints

- Use integer minor units for prices, inserted cash, and change; do not use floating-point values.
- First model one in-memory machine and one active transaction at a time.
- Cash inventory must be updated atomically with product stock for a successful purchase.

### Test scenarios

- Buy an in-stock item with exact cash.
- Buy an in-stock item and receive exact change in supported denominations.
- Reject a sold-out or unknown slot without charging the customer.
- Reject changing the selected slot after cash has been inserted.
- Cancel after inserting cash and receive the full inserted amount back.
- Reject completion with insufficient payment and preserve product stock.
- Reject completion when exact change cannot be made and refund the inserted cash.

### Interview follow-ups

- How would you add card, UPI, or wallet payments without changing the purchase flow?
- How would you support restocking and cash collection safely while purchases occur?
- How would multiple machines share catalogue, pricing, and inventory data?
- How would you prevent two concurrent purchases from vending the final item?
- What changes if a transaction can contain multiple selected items?

### Requirements

- A machine owns item slots and its cash inventory.
- Each slot has a unique code, product details, a positive price in integer minor units, and available stock.
- A customer can start one transaction, select one slot, and insert supported cash denominations.
- Once cash has been inserted, changing the selected slot requires cancellation and a new transaction.
- Reject an unknown or sold-out slot without dispensing a product.
- Reject unsupported denominations without changing the inserted total.
- A purchase succeeds only when inserted cash covers the price and the machine can return exact change.
- On success, dispense one product, decrement its stock once, return the change denominations, record the
  transaction result, and clear the active transaction.
- A customer can cancel before completion. Cancellation returns inserted cash and clears the active
  transaction without changing product stock.
- If a purchase cannot complete because payment is insufficient or exact change is unavailable, do not
  decrement product stock; return the inserted cash and clear the transaction.


## Candidate model sketch

This is an evolving entity sketch, including payment follow-ups beyond the cash-only exercise. Keep the base
implementation cash-only; use the [design notes](../design/) to decide when each extra responsibility is earned.

```text

- Machine
- Slot
- CashInventory

CLASS DIAGRAM

MachineService
- machines : Map<int, Machine>
- paymentResolver : PaymentResolver

+ selectItem(machineId, slotId)
+ startPayment(machineId, paymentType) : PaymentInitResult
+ insertCash(machineId, denomination)
+ completePurchase(machineId) : PaymentResult
+ cancel(machineId)


Machine
- id : int
- slots : Map<int, Slot>
- cashInventory : Map<CashDenomination, int>
- activeTransaction : Transaction
- pastTransactions : List<Transaction>

+ addSlot(slot)
+ addItemToSlot(slotId, count)
+ updateCashInventory(denomination, count)


Transaction
- id : long
- selectedSlotId : int
- payment : Payment
- state : TransactionState


TransactionState
- IN_PROGRESS
- SUCCESS
- FAILED
- CANCELLED


Slot
- id : int
- itemName : String
- itemCount : int
- itemCost : int

+ addStock(count)
+ decreaseStock(count)
+ updateItem(name, cost)


Payment
- id : long
- amount : int
- type : PaymentType
- status : PaymentStatus
- details : PaymentDetails


PaymentType
- CASH
- UPI
- CARD


PaymentStatus
- INITIATED
- PENDING_CONFIRMATION
- SUCCESS
- FAILED
- REFUNDED


PaymentDetails <<interface>>


CashPaymentDetails implements PaymentDetails
- collectedCash : Map<CashDenomination, int>


UpiPaymentDetails implements PaymentDetails
- externalPaymentId
- qrCode


CardPaymentDetails implements PaymentDetails
- externalPaymentId


CashDenomination <<enum>>
- ONE
- FIVE
- TEN
  ...


PaymentProcessor <<interface>>
+ initiatePayment(amount) : Payment
+ validatePayment(payment, machine) : PaymentResult
+ refund(payment)


CashPaymentProcessor implements PaymentProcessor
+ insertCash(payment, denomination)
- validateEnoughCash(payment)
- calculateChange(payment, cashInventory)


UpiPaymentProcessor implements PaymentProcessor
+ handlePaymentCallback(paymentId, status)


CardPaymentProcessor implements PaymentProcessor
+ handlePaymentCallback(paymentId, status)


PaymentResolver
- processors : Map<PaymentType, PaymentProcessor>

+ resolve(paymentType) : PaymentProcessor


PaymentResult
- change : Map<CashDenomination, int>

PaymentInitResult
- paymentId
- paymentType
- paymentSpecificDetails
```
