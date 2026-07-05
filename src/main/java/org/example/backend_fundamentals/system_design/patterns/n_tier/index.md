---
order: 20
---

# N-Tier Architecture

Foundational vocabulary for where components sit in a request path. Interviewers rarely ask "explain N-tier" directly — but "layer vs tier" is a common warm-up, and any "design X" answer naturally becomes an N-tier diagram.

## Layer vs tier (the distinction that gets asked)

- **Layer = logical separation** inside the codebase: Controller → Service → Repository. All may run in the **same JVM process**. This is *layered* architecture.
- **Tier = physical separation** across machines: Browser → Web server → App server → Database server. Each can run on separate infrastructure and scale independently.

So three layers can run on one tier, and one logical layer can be spread across several tiers. Layer is about code organization; tier is about deployment.

## The tiers

- **1-tier:** app + logic + DB on one machine (Java app + embedded SQLite). No network hop, simple, not scalable, single point of failure.
- **2-tier:** client talks **directly** to the database (Swing app → MySQL, business logic + SQL in the client). Simple, but the DB is exposed to clients, logic is hard to maintain, and it's a security risk.
- **3-tier:** Presentation → Business → Data (Browser → Spring Boot API → MySQL). The standard web shape. The backend becomes the **gatekeeper**: clients can't run arbitrary SQL, business rules live in one place, and each tier scales independently.

The middle tier is the whole point: removing it (Browser → DB) means anyone can run SQL, rules scatter, and the DB faces the internet.

## Closed vs open layers

- **Closed layers:** a request must pass through **every** layer in order (Presentation → Business → Data; you can't jump Presentation → Data). Cleaner separation, at the cost of extra hops.
- **Open layers:** a layer may be skipped (Presentation → Data directly). Faster, but introduces tighter coupling.

Default to closed; open a layer only as a deliberate performance exception.

## Modern N-tier request path

The system-design building blocks (studied separately) slot into the path like this:

```text
User → CDN → Load Balancer → API Gateway → Application Servers → Cache (Redis) → Database
```

Each component scales independently. N-tier is mostly a map of *where* CDN, LB, cache, replication, and sharding sit relative to the request — not a separate technology.

## Quick recall

**Q. Layer vs tier?**
A. Layer = logical separation in code (Controller/Service/Repository, often one process); tier = physical separation across machines/servers.

**Q. Why add a middle tier (3-tier) instead of client → DB?**
A. The backend is the gatekeeper — it hides the DB from clients, centralizes business rules, enforces security, and lets each tier scale independently.

**Q. Closed vs open layers?**
A. Closed = must traverse every layer in order (clean, extra hops); open = may skip layers (faster, more coupling).

**Q. Do interviewers ask "explain N-tier"?**
A. Rarely directly — but "design Amazon/Netflix/WhatsApp" answers become N-tier diagrams, and "layer vs tier" is a frequent warm-up.
