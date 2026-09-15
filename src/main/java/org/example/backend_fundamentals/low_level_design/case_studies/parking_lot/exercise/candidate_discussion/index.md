---
search: false
---

# Candidate discussion — Parking Lot

Start with a small allocation and release flow:

> “I’ll model one lot with floors and typed spots. A vehicle gets one compatible available spot; exiting
> releases it. I’ll leave pricing and payment outside the first pass unless they are required.”

## Questions to ask

1. Which vehicle types and spot compatibility rules should the first version support?
2. Does a lot have multiple floors, and should allocation choose the first available compatible spot?
3. What identifies a vehicle and the allocated spot at exit?
4. Should the first implementation calculate payment, create tickets, or model gates and reservations?
5. Is concurrent allocation of the final available spot in scope now?

## Agreed scope for this exercise

- One lot has one or more floors with fixed, typed parking spots.
- A vehicle parks only in an available compatible spot; full capacity is rejected explicitly.
- Exit releases a spot by its identifier and availability is reported by vehicle type.
- The runnable version is in memory and single-threaded.
- Payment, pricing, tickets, gates, reservations, persistence, and concurrency are follow-ups.

The final [exercise](../) records the exact model and acceptance criteria.
