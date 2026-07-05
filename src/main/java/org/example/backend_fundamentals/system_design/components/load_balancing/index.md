---
order: 30
---

# Load Balancing — L4 vs L7, DNS Routing, Redundancy

## How it works

### Why load balancing exists

A load balancer sits in front of multiple servers and distributes incoming traffic across them. Goals:

- **Scalability** — handle more traffic by adding servers
- **Availability** — traffic continues if a server fails
- **Fault tolerance** — no single machine becomes a bottleneck

```text
Users → Load Balancer → [Server A | Server B | Server C]
```

The load balancer decides which backend server receives each request.

### Layer 4 vs Layer 7 — the key distinction

**Layer 4 (transport layer)** routes using source/destination IP, ports, and TCP/UDP metadata. It does NOT inspect HTTP request contents — it only sees `IP + port` and forwards the connection. Think: *"Who is talking to me?"*

**Layer 7 (application layer)** can inspect URL paths, HTTP headers, cookies, and hostnames. Think: *"What are they asking for?"*

```text
/api/*    → API servers
/admin/*  → Admin servers
```

Layer 7 is application-aware.

### Combining L4 and L7

One layer makes broad routing decisions; another makes application-specific ones:

```text
User → Global routing (L4-style) → Singapore region → NGINX / ALB (L7) → microservices
```

Example: a user from Malaysia is routed to the Singapore region, then L7 routing splits `/products`, `/admin`, `/api` to the correct backend services.

### DNS-based load balancing

DNS can participate in load balancing by returning different IPs depending on geography, availability, or traffic distribution — e.g. a user in Malaysia gets a Singapore IP and connects directly to that region. DNS is often the **first level of global traffic distribution**. (Details in `networking/dns/DnsResolution.md`.)

### Software vs hardware load balancers

For interviews, software load balancers matter far more: **NGINX, HAProxy, AWS ALB** — these are what backend engineers actually touch. Hardware load balancers exist; know *that* they exist, but spend prep time on software.

### NGINX

Commonly used as a Layer 7 load balancer — inspects paths/headers/cookies and routes accordingly (`/api/* → cluster A`, `/admin/* → cluster B`). NGINX can also operate at Layer 4, but its most-discussed use case is L7 routing.

### Self-managed vs cloud-managed

- **Self-managed (NGINX you install):** you own configuration, deployment, health checks, scaling.
- **Cloud-managed (AWS ALB):** you configure routing rules; the provider manages availability, scaling, health monitoring.

Interviewers care that you understand the concept, not every cloud-specific setting.

### Consistent hashing for routing

Consistent hashing maps a request to a particular server based on a key (e.g. `hash(userId) → Server B`) — the same user keeps reaching the same destination.

**Modern use case:** historically explained via sticky sessions (user's cart in Server B's memory). Modern systems avoid in-memory session affinity by storing state in Redis/databases/shared storage. The more realistic example today is **distributed cache node selection** — the same user's cached data consistently lands on the same Redis node instead of spreading across cache servers. (Mechanics in `system_design/caching/CachingAndDistributedCache.md`.)

### Load balancer redundancy

The load balancer itself can become a single point of failure — if the only LB dies, the entire system is unavailable. Solution:

```text
Users → DNS → [LB-A | LB-B] → servers
```

Multiple load balancers + health checks + failover. **The load balancer itself must be highly available.**

### DNS redundancy — the natural follow-up

"What if DNS fails?" DNS providers are themselves distributed: records are served from many DNS servers globally, responses are cached, and multiple servers serve the same records. DNS is designed to avoid being a single point of failure.

## Gotchas / Trick questions

1. **"Layer 4 means regional routing."** Not exactly — that's a useful intuition, but L4 is defined by the *information it uses* (IPs, ports, TCP/UDP metadata). Choosing a region is just one possible use of it.
2. **"DNS load balancing and L4 load balancing are the same."** Not quite — DNS-based routing happens *before* any load balancer is reached (User → DNS decision → regional endpoint → LB → servers). DNS participates in global distribution but is not itself an L4 load balancer.
3. **"If DNS already sends users to the right region, why another load balancer?"** DNS only picks the regional entry point. Inside the region there may be 10–500 backend servers — a load balancer still distributes among them.
4. **"Consistent hashing is mainly for sticky sessions."** Historically yes; today the stronger example is distributed caches (Redis cluster). The session example is valid but less representative of modern architectures.
5. **"Do we keep adding load balancers in front of load balancers forever?"** No — that would be infinite recursion. Redundancy comes from multiple LBs + health checks + failover under DNS, not an endless chain.
6. **"Then who protects DNS?"** DNS infrastructure is already globally distributed and replicated — the answer is not a load balancer in front of DNS.

## Performance characteristics

| | Layer 4 | Layer 7 |
|---|---------|---------|
| Pros | Faster, less inspection work, lower overhead | Path/header/cookie-based routing |
| Cons | Cannot route on application data | Must inspect requests — more processing overhead |

## Good to know

### What interviewers usually care about (SDE2)

Why load balancing exists · L4 vs L7 · DNS-based routing · NGINX as software LB · consistent hashing concept · LB redundancy. Product-specific configuration knowledge is not expected — mechanism and trade-offs beat vendor features.

## Quick recall

**Q. What information does a Layer 4 load balancer use?**
A. IP addresses, ports, and transport-layer metadata.

**Q. What information does a Layer 7 load balancer use?**
A. URL paths, headers, cookies, hostnames — application-layer data.

**Q. Why can DNS be considered a form of load balancing?**
A. It can return different IPs based on geography, availability, or traffic distribution.

**Q. If DNS routes me to Singapore, why do I still need a load balancer there?**
A. DNS only chooses the regional entry point; the regional LB distributes traffic among the backend servers.

**Q. What is the core idea behind consistent hashing?**
A. The same key (e.g. user ID) consistently maps to the same backend node.

**Q. What is a realistic modern use case for consistent hashing?**
A. Routing data to the correct node in a distributed cache such as Redis.

**Q. How do we avoid the load balancer becoming a single point of failure?**
A. Deploy multiple load balancers with health checks and failover routing traffic to healthy ones.
