# Availability vs Reliability vs Fault Tolerance

## How it works

### The three definitions

| Concept | Question it answers | Property of |
|---------|--------------------|-------------|
| **Reliability** | How often does a component fail? | The component itself |
| **Availability** | Can users use the service *right now*? | The service |
| **Fault tolerance** | Can the system keep operating *while* failures occur? | The system design |

**Reliability** — frequency/probability of failure; how long a component runs before breaking. A server that runs 5 years without crashing is highly reliable. *Reliable = breaks rarely.*

**Availability** — whether the system can continue serving traffic: the website responds, users can send messages, customers can place orders. *Available = users can use it.*

**Fault tolerance** — designed so that when a failure occurs, the system keeps working and users don't notice.

### Reliability helps availability

Fewer failures → less downtime → higher availability. That's why reliability *contributes* to availability.

### Availability does NOT require reliability

A system can be highly available even if components fail frequently. 100 app servers with 1 crashing every hour sounds unreliable — but if the load balancer removes failed servers and traffic shifts, users never notice:

```text
Component reliability = low
System availability  = high
```

This is one of the most important distributed-systems concepts.

### Reliability does NOT guarantee availability

A single database server that crashes only once every 3 years is highly reliable — but when it crashes, the entire application stops for hours:

```text
Reliability  = high
Availability = low
```

### Fault tolerance = availability in the presence of failures

Availability asks "can users use the system?" Fault tolerance asks "can users still use it *while things are failing*?"

## High Availability vs Fault Tolerance

**High Availability (HA)** — goal: very little downtime. Primary DB fails → replica promoted in ~30 seconds. Users may see a short outage, a few failed requests, a brief interruption.

**Fault Tolerance (FT)** — goal: near-zero *visible* interruption. Primary fails → replica takes over immediately; users keep working, no noticeable downtime.

```text
HA:  failure → small interruption → recovery
FT:  failure → service continues  → users don't notice
```

## Practical examples

| Setup | Reliability | Availability | Fault tolerance | Why |
|-------|-------------|--------------|-----------------|-----|
| 1 app server, crashes once per 5 yrs | High | Low | None | When the server dies, the entire service dies |
| LB + 100 servers, failures common | Average per server | Very high | Moderate | Traffic shifts automatically; users continue |
| Primary + replica DB, promote on failure | Depends | High | Partial | Brief interruption still exists during promotion |
| Multi-node cluster, one node fails | Not necessarily perfect | Extremely high | High | Designed assuming failures will happen |

## Gotchas / Trick questions

1. **"If a system is reliable, is it automatically highly available?"** No — a single DB server failing once every 3 years is reliable, but its one failure takes the whole application down for hours.
2. **"If a system is highly available, is it automatically reliable?"** No — 100 servers with one crashing every hour are individually unreliable, yet the load balancer keeps the service available.
3. **"Availability and reliability are the same thing."** Reliability measures failures (component focus); availability measures whether users can use the service (service-continuity focus).
4. **"Fault tolerance means failures never happen."** Failures absolutely happen — fault tolerance means the system continues operating *despite* them.
5. **"If there's even a tiny interruption, is it still fault tolerant?"** Nuance: in theory FT aims for no visible interruption; in practice perfect zero impact is extremely difficult — a few failed requests, millisecond-level disruption, or tiny switchover delays may remain. The goal is making failures *effectively invisible* to users.
6. **Understanding check (correct):** HA = system works again after a short interruption; FT = system continues without users noticing.

## Performance characteristics

### How availability is increased

Through **redundancy**: multiple servers, multiple replicas, multiple nodes, automatic failover, load balancing.

Key insight: modern distributed systems **assume hardware failures will happen**. The goal is not preventing all failures — it's *surviving* them.

## Good to know

### Hospital analogy

- **Hospital A:** 1 doctor who never takes leave — doctor reliability high, hospital availability low (doctor sick → hospital closes).
- **Hospital B:** 50 doctors, some always sick or on leave — individual reliability lower, hospital availability very high (always open).

That's exactly how distributed systems use redundancy.

### Netflix-style thinking

Large distributed systems expect servers, disks, and networks to fail. Design philosophy: **failures are inevitable; downtime is optional.** The system stays available because failures are isolated and traffic is redirected.

## Quick recall

**Q. What does reliability measure?**
A. How often a component fails.

**Q. What does availability measure?**
A. Whether users can use the service right now.

**Q. Does high reliability guarantee high availability?**
A. No — a single highly reliable server still creates downtime when it eventually fails.

**Q. Does high availability guarantee high reliability?**
A. No — components may fail frequently while redundancy keeps the service running.

**Q. High availability vs fault tolerance?**
A. HA allows small interruptions; FT aims for no noticeable interruption.

**Q. What is the core distributed-systems mindset?**
A. Assume failures will happen and design the system to continue operating.
