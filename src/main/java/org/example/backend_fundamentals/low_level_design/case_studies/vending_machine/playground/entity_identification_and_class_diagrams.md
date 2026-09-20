# Vending Machine — entity identification and class diagrams

## Goal

Model the smallest useful in-memory vending machine for one-item cash purchases.

## Requirements

- A machine owns item slots and its cash inventory.
- Each slot has a unique code, product details, a positive price in integer minor units, and available stock.
- A customer can start one transaction, select one slot, and insert supported cash denominations.
- Reject an unknown or sold-out slot without dispensing a product.
- Reject unsupported denominations without changing the inserted total.
- A purchase succeeds only when inserted cash covers the price and the machine can return exact change.
- On success, dispense one product, decrement its stock once, return the change denominations, record the
  transaction result, and clear the active transaction.
- A customer can cancel before completion. Cancellation returns inserted cash and clears the active
  transaction without changing product stock.
- If a purchase cannot complete because payment is insufficient or exact change is unavailable, do not
  decrement product stock; return the inserted cash and clear the transaction.

## Constraints

- Use integer minor units for prices, inserted cash, and change; do not use floating-point values.
- First model one in-memory machine and one active transaction at a time.
- Cash inventory must be updated atomically with product stock for a successful purchase.

## Test scenarios

- Buy an in-stock item with exact cash.
- Buy an in-stock item and receive exact change in supported denominations.
- Reject a sold-out or unknown slot without charging the customer.
- Cancel after inserting cash and receive the full inserted amount back.
- Reject completion with insufficient payment and preserve product stock.
- Reject completion when exact change cannot be made and refund the inserted cash.

## Interview follow-ups

- How would you add card, UPI, or wallet payments without changing the purchase flow?
- How would you support restocking and cash collection safely while purchases occur?
- How would multiple machines share catalogue, pricing, and inventory data?
- How would you prevent two concurrent purchases from vending the final item?
- What changes if a transaction can contain multiple selected items?

## Entity identification

## Class diagrams
