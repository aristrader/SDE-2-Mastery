---
search: false
---

# Candidate discussion — Parking Lot

Start by narrowing the intentionally vague prompt:

> “I’ll model one lot with floors and typed spots. A vehicle gets one compatible available spot; exiting
> releases it. I’ll leave pricing and payment outside the first pass unless they are required.”

## Sample clarification conversation

**Candidate:** “Which vehicle types and compatibility rule should I support?”

**Interviewer:** “Cars, bikes, and trucks. Keep the first version exact-match.”

**Design decision:** `ParkingSpot` owns one `VehicleType`; `canFit` uses that rule.

**Candidate:** “Does the lot have multiple floors, and do we need nearest-spot selection?”

**Interviewer:** “Use multiple floors. First compatible available spot is enough.”

**Design decision:** a floor searches its spots and the lot searches floors in order; no allocation strategy abstraction yet.

**Candidate:** “What must survive from entry to exit?”

**Interviewer:** “Return a ticket with the assigned spot, floor, gate, and entry time. Quote an hourly rate and release after payment.”

**Design decision:** the service owns the entry/exit workflow; `Ticket` links the spot to pricing and payment.

**Candidate:** “Do I need a payment gateway or multi-instance concurrency?”

**Interviewer:** “No. Log a successful payment in memory and explain the production boundary.”

**Design decision:** keep the gateway as a stub. JVM-local synchronization is enough for the exercise; durable atomic allocation and idempotency are follow-ups.

## Agreed scope for this exercise

- One lot has one or more floors with fixed, typed parking spots.
- A vehicle parks only in an available compatible spot; full capacity is rejected explicitly.
- Entry returns a ticket; exit quotes an hourly fee, logs a stub payment, closes the ticket, and releases its spot.
- The runnable version is in memory and only protects concurrent allocation within one JVM.
- Reservations, a real payment gateway, persistence, and multi-instance concurrency are follow-ups.

The final [exercise](../) records the exact model and acceptance criteria.
