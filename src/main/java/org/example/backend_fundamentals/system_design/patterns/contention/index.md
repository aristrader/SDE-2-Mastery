---
order: 50
---

# Dealing With Contention

Use this pattern when concurrent requests can make conflicting changes to the same logical resource: the last concert seat, remaining inventory, a bank balance, an auction bid, or an available driver.

## Escalation path

| Situation | Smallest correct mechanism | Why |
|---|---|---|
| One durable record decides the result | Unique constraint, conditional update, or optimistic version | The database atomically selects one winner |
| Short transaction needs exclusive access | Row lock / pessimistic transaction | Simple while one database owns the resource |
| Short-lived cross-instance work ownership | Lease with TTL | Coordinates work, but is not final business truth |
| Order matters more than immediate response | Partitioned queue / serialized owner | Removes concurrent writers for the same key |
| Multiple services own one business decision | State machine, saga/compensation, and reconciliation | Makes cross-service failure explicit |

Start with the database if a single database owns the resource. Do not introduce a distributed lock before checking whether one conditional write solves the race.

## The durable-winner rule

```text
temporary coordination:  acquire lease for item X
final decision:          UPDATE item SET status = CLAIMED WHERE status = AVAILABLE
recovery:                retry idempotently and reconcile expired/inconsistent work
```

A Redis lock can reduce duplicate work, but its expiry does not prove the original worker failed. The durable conditional transition or constraint decides whether a sale, reservation, or assignment actually happened.

## Useful simplifications

- Batch requests into time windows when immediate per-request ordering is not a product requirement.
- Serialize writes by resource key through a partitioned queue when throughput permits.
- Reduce the contention domain: reserve a seat, not an entire event; lock one driver, not the whole city.
- Make the client command idempotent so timeouts and retries do not create a second claim.

## Quick recall

**Q. What is the first answer to double booking?**
A. A durable conditional update or constraint in the database that owns the booking.

**Q. When does a distributed lock help?**
A. For a brief ownership window across instances; still validate durable state before committing the business outcome.
