---
order: 70
---

# System Design: Disaster Recovery (DR)

## Definition

Disaster Recovery (DR) is the process of restoring infrastructure, services, and data after major failures such as:
- Natural disasters, fires, floods, power outages
- Cyber attacks, business disruptions
- Data center failures, cloud region outages

Core idea:
- Production systems fail.
- Business must recover quickly.
- Data should not be permanently lost.
- Services should resume operation within acceptable limits.

DR primarily relies on:
- Data replication
- Data backups
- Secondary locations
- Failover mechanisms

Disaster Recovery answers two critical metrics:
1. How quickly can we recover? (**RTO**)
2. How much data can we afford to lose? (**RPO**)

---

## RTO (Recovery Time Objective)
Maximum acceptable downtime.
Maximum acceptable delay between service interruption and service restoration.
Question RTO answers: *If the system dies, how long can it remain unavailable?*

Example RTOs:
- **Netflix**: Few minutes
- **Payroll System**: Several hours
- **Internal Reporting Dashboard**: Up to a day

Key Memory Trick: RTO = Time to recover (Downtime allowed).

---

## RPO (Recovery Point Objective)
Maximum acceptable data loss.
Maximum acceptable amount of time since the last recoverable data point.
Question RPO answers: *If disaster happens, how much recent data can be lost?*

Examples:
- Nightly backup at 12 AM. Disaster at 6 PM. Data loss is 18 hours. RPO = 18 hours.
- Replication every minute. Worst-case data loss = 1 minute. RPO = 1 minute.

Key Memory Trick: RPO = Data loss allowed.

---

## Backup vs Replication

**Misconception:** Replication = Backup.
**Correction:** Replication and backup solve different problems.

### Replication
Replication copies current state. If someone executes `DELETE USERS;`, both the primary and the replica become corrupted because replication copies changes.

Replication:
- Improves availability
- Reduces RPO
- Does NOT replace backups (corruption/accidental deletion replicate)

### Backup
Backup stores historical recoverable states. Backups preserve history.

---

## Disaster Recovery Strategies

Strategies range from cheap (slow recovery, high data loss) to expensive (fast recovery, low data loss).

### Strategy 1: Backup Only
Architecture: Production DB → Periodic Backup Storage
Recovery: Restore backup, rebuild services, bring system online.
- Pros: Cheap.
- Cons: Large downtime, large data loss.
- Typical RTO: Hours to days. RPO: Hours.

### Strategy 2: Cold Site
Secondary site exists but infrastructure is mostly inactive.
Recovery: Start infrastructure, restore backups, bring services online.
- Pros: Cheaper than warm/hot sites.
- Cons: Recovery is slow.
- Typical RTO: Hours to days. Example: Recreate via Terraform in a new region.

### Strategy 3: Warm Site
Infrastructure already exists (Servers running, databases replicated), but not actively serving production traffic.
Recovery: Switch traffic, promote replica.
- Pros: Much faster recovery.
- Cons: More expensive than cold site.
- Typical RTO: Minutes. RPO: Minutes or seconds.

### Strategy 4: Hot Site
Both regions active and synchronized. Both serve users.
Recovery: Traffic just continues on the active region.
- Pros: Very low downtime and data loss.
- Cons: Most expensive, operationally complex.
- Typical RTO: Seconds. RPO: Near zero.

---

## Cost vs Recovery Tradeoff
Lower RTO and lower RPO always require more money.
Backup Only -> Cold Site -> Warm Site -> Hot Site (Cost: Low -> High; RTO/RPO: High -> Low)
