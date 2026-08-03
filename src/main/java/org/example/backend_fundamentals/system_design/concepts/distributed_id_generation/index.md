---
order: 80
---

# Distributed ID Generation

## How it works

The interview problem is simple: many machines must generate IDs without collisions, with low latency, and without one database sequence becoming the bottleneck.

Common requirements:

- unique
- numeric
- fits in 64 bits
- roughly time-sortable
- high throughput, e.g. 10K+ IDs/second

## Common options

| Approach | Good | Problem |
|----------|------|---------|
| Database `AUTO_INCREMENT` | Simple on one database | One DB becomes the coordinator; hard across shards/regions |
| Multi-master increments | Each DB increments by a step, e.g. DB1: 1,3,5 and DB2: 2,4,6 | IDs are not globally time-ordered; adding/removing DBs is awkward |
| UUID | No coordination; easy to generate anywhere | 128-bit, often non-numeric, not naturally ordered unless using time-ordered variants |
| Ticket server | Numeric and simple | Central service can become a SPOF/bottleneck unless replicated carefully |
| Snowflake-style ID | 64-bit, time-sortable, distributed | Requires machine/datacenter ID assignment and clock discipline |

For a basic system-design answer, Snowflake-style IDs are the useful pattern to know.

## Snowflake-style layout

A 64-bit ID is split into fields:

```text
sign | timestamp | datacenter | machine | sequence
 1   |    41     |     5      |    5    |   12
```

Meaning:

- **Sign bit:** usually `0`, reserved.
- **Timestamp:** milliseconds since a custom epoch. This makes newer IDs larger than older IDs.
- **Datacenter ID:** identifies the region/datacenter.
- **Machine ID:** identifies the generator machine inside that datacenter.
- **Sequence:** counter for multiple IDs generated in the same millisecond on the same machine.

With 5 datacenter bits and 5 machine bits:

```text
2^5 = 32 datacenters
2^5 = 32 machines per datacenter
```

With 12 sequence bits:

```text
2^12 = 4096 IDs per machine per millisecond
```

The timestamp field being 41 bits gives roughly 69 years from the custom epoch:

```text
2^41 milliseconds ~= 69 years
```

Use a custom epoch near the product launch time so those 69 years start from a useful date.

## Gotchas / Trick questions

1. **"Why not just use `AUTO_INCREMENT`?"** It is fine on one DB, but distributed systems need many writers without a single coordination point.
2. **"Why not UUID?"** UUID is great when opacity and no coordination matter, but it may fail numeric/64-bit/sortable requirements.
3. **"What can break Snowflake?"** Clock rollback can generate duplicate or out-of-order IDs if a machine's timestamp moves backward.
4. **"Can you change machine IDs casually?"** No. Datacenter/machine IDs must be assigned carefully; accidental reuse can create collisions.
5. **"Are Snowflake IDs strictly globally ordered?"** They are roughly time-ordered. IDs generated in the same millisecond across machines may not reflect exact real-world order.

## Quick recall

**Q. When is DB auto-increment enough?**  
A. Single database or low-scale centralized writes.

**Q. Why does distributed ID generation need coordination?**  
A. Without coordination or unique machine ranges, two machines can emit the same ID.

**Q. What is the Snowflake mental model?**  
A. Timestamp + datacenter ID + machine ID + per-millisecond sequence.

**Q. Why are Snowflake IDs sortable?**  
A. The high-order timestamp bits grow over time.

**Q. Biggest Snowflake operational risk?**  
A. Clock skew/rollback and duplicate machine IDs.
