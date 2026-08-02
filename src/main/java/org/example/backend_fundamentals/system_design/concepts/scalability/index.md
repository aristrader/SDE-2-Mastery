---
order: 10
---

# Scalability, Redundancy, and Sequential vs Random Access

## How it works

### What is Scalability?

Scalability is the ability of a system to handle increasing load without significant performance degradation.

The load can be:

- More users
- More requests
- More data
- More traffic spikes

The entire purpose of many system design components is scalability:

```text
DNS
↓
Load Balancers
↓
Caching
↓
Replication
↓
Sharding
```

These are all mechanisms to allow systems to continue operating as load grows.

### Start with the request path

Before adding components, be able to explain the simplest path:

```text
User enters api.example.com
        ↓
DNS returns an IP address
        ↓
Browser/mobile app sends HTTP request
        ↓
Web server returns HTML or JSON
```

At very small scale, the web app, API logic, cache, and database can all live on one machine. That is not "bad design"; it is the simplest design that works. The system-design skill is knowing when the next bottleneck appears and which component removes it.

---

### Vertical Scaling (Scale Up)

Increase the resources of a single machine.

Example:

```text
Before:
4 CPU
16 GB RAM

After:
32 CPU
128 GB RAM
```

The machine remains the same; it just becomes more powerful.

#### Advantages

- Simple architecture
- Minimal code changes
- Easier consistency management
- No distributed system complexity

#### Disadvantages

- Hardware limits eventually reached
- Expensive
- Often requires downtime
- Single point of failure remains

---

### Horizontal Scaling (Scale Out)

Add more machines instead of making one machine larger.

```text
           LB
         /  |  \
       S1  S2  S3
```

Traffic is distributed among multiple servers.

#### Advantages

- Higher capacity
- Better fault tolerance
- Easier incremental growth
- Commodity hardware

#### Disadvantages

Creates new distributed-system problems:

- Load balancing
- Session management
- Data consistency
- Database bottlenecks

---

### Typical Scaling Evolution

A common growth path for real systems:

#### Stage 1

```text
Users
 |
App
 |
DB
```

Everything may still be on one server. This is fine for a toy product or early prototype, but it has two obvious limits: one machine must handle both application traffic and database work, and one machine failure takes everything down.

#### Stage 2

```text
Users
 |
LB
 |
App App App
 |
DB
```

The web tier can now scale horizontally behind a load balancer. This helps only if app servers are interchangeable; if one app server stores user session data locally, requests cannot freely move between servers.

#### Stage 3

```text
Users
 |
LB
 |
Apps
 |
Primary DB
   |
Replicas
```

Read replicas reduce pressure on the primary database when reads dominate writes. Writes still go to the primary; reads can be routed to replicas if the application can tolerate replication lag.

#### Stage 4

```text
Apps
 |
Cache
 |
DB
```

Cache removes repeated reads from the database. It helps most when data is read often and updated less often. It adds a new correctness question: how stale can cached data be?

#### Stage 5

```text
Shard 1
Shard 2
Shard 3
Shard 4
```

Sharding is usually a later move. It scales storage and write throughput, but it introduces shard-key choice, resharding, cross-shard joins, and hotspot risk.

#### Stage 6

```text
Logs / Metrics / Alerts / CI-CD
```

Once the system has multiple tiers, regions, caches, queues, and shards, production work is no longer only architecture boxes. You need centralized logs for debugging, metrics for system health and business health, alerts for symptoms users feel, and automated build/test/deploy so every region runs compatible code and configuration.

This progression is essentially the story of scaling a system.

### Interview scaling sequence

When asked to scale a simple product from one user to millions, do not jump directly to microservices. Walk the interviewer through the pressure point that forces each move:

| Pressure | Smallest useful move |
|----------|----------------------|
| One box runs app + DB | Split web tier and database tier |
| App server is overloaded or a SPOF | Add load balancer + more app servers |
| Database reads dominate | Add read replicas |
| Repeated expensive reads | Add cache |
| Static assets are slow globally | Move images/CSS/JS/video to CDN |
| App servers keep sessions locally | Move session/state to shared storage |
| One region is too far or risky | Add multi-region routing + replication |
| Slow work blocks requests | Add queue + workers |
| One database cannot hold traffic/data | Shard by a stable, high-cardinality key |
| Many tiers/regions make failures hard to see or roll out | Add centralized logging, metrics, alerts, and deployment automation |

The senior signal is explaining **why now** for each component. Every new box solves a concrete bottleneck and adds an operational cost.

### SQL vs NoSQL in this scaling story

The default early answer is usually a relational database because relationships, joins, constraints, and transactions are valuable. Do not switch to NoSQL just because the word "scale" appears.

NoSQL becomes a strong candidate when the access pattern is simple and high-volume: key-value lookups, document reads/writes, wide-column event data, graph traversal, or massive semi-structured data where joins are not central.

Interview framing:

```text
Relational data + joins + constraints → SQL first
Simple lookup / document access / massive flexible data → consider NoSQL
```

The sharper answer is not "SQL vs NoSQL." It is "what does the read/write path need?"

---

## Redundancy vs Scalability

### The Important Distinction

Scalability and redundancy are not the same thing.

| Goal | Question |
|--------|--------|
| Scalability | Can I handle more load? |
| Redundancy / Availability | Can I survive failures? |

A design can achieve one without achieving the other.

---

### Why Horizontally Scaled App Servers Are Considered Redundant

Consider:

```text
        LB
      /  |  \
    S1  S2  S3
```

All servers run the same application.

Every server can handle any request.

If S2 fails:

```text
        LB
      /     \
    S1      S3
```

The system still functions.

Why?

Because S1 and S3 can perform the same job as S2.

The functionality is duplicated.

This is redundancy.

---

### Active Redundancy

A common misconception is:

> Redundancy means an idle backup machine waiting for failure.

Example:

```text
Primary
  |
Standby
```

This is passive redundancy.

Modern systems usually use:

```text
S1  S2  S3
```

All servers actively serve traffic.

They simultaneously provide:

- More capacity
- Load sharing
- Failure tolerance

This is active redundancy.

---

### Why Sharding Does NOT Automatically Provide Redundancy

Consider:

```text
User 1-1000     → Shard A
User 1001-2000  → Shard B
User 2001-3000  → Shard C
```

Each shard owns different data.

If Shard B fails:

```text
Shard A → Alive
Shard B → Dead
Shard C → Alive
```

Users 1001-2000 are unavailable.

Why?

Because:

```text
Shard A cannot replace Shard B
Shard C cannot replace Shard B
```

No functionality is duplicated.

Therefore:

```text
Scalability = Yes
Redundancy = No
```

---

### How Real Systems Add Redundancy to Shards

Production systems typically combine:

```text
Shard A
 ├─ Primary
 └─ Replica

Shard B
 ├─ Primary
 └─ Replica

Shard C
 ├─ Primary
 └─ Replica
```

Now:

```text
Shard B Primary fails
```

Replica takes over.

Result:

```text
Scalability from sharding
+
Redundancy from replication
```

---

## Stateless Web Tier

Horizontal scaling of app servers only stays simple when any server can handle any request:

```text
Client → LB → App 1
Client → LB → App 2
Client → LB → App 3
```

If session data lives only in `App 1`, the load balancer must keep that user pinned there with sticky sessions. That works for small systems, but it makes server removal, autoscaling, and failure handling harder.

Example failure:

```text
User A session lives on App 1
App 1 dies
LB sends User A to App 2
App 2 has no session data
→ user appears logged out or request fails
```

The usual production fix is to move state out of the web tier:

```text
Client → LB → any App → Redis/DB/session store
```

Now app servers are replaceable workers. Autoscaling can add or remove them based on traffic without migrating user sessions.

This is why "stateless" does not mean the product has no state. It means **the app server does not own state locally**. The state still exists, but it lives in a shared store that every app server can reach.

### Sticky sessions vs shared session store

| Approach | Benefit | Problem |
|----------|---------|---------|
| Sticky sessions | Simple; fewer shared-store reads | Server failure loses/pins sessions; rebalancing is harder |
| Shared session store | Any app server can handle any request | Adds a dependency and shared-store latency |

For interviews, prefer stateless app servers unless there is a strong reason not to.

---

## Multi-Region Scaling

Multi-region is not just "deploy the same app twice." The hard parts are routing, data, and operations:

- **Traffic routing:** GeoDNS or global load balancing sends users to the nearest healthy region.
- **Data synchronization:** failover is useless if the backup region does not have the user's data.
- **Cache behavior:** regional caches may be warm for local users and cold after failover.
- **Deployment consistency:** config, schema, and service versions must stay compatible across regions.

Interview answer: start single-region, then add multi-region when latency, disaster recovery, or business continuity justify the complexity.

### Failover mental model

Normal traffic:

```text
India users → Singapore region
US users    → US-East region
```

If Singapore is down:

```text
India users → US-East region
```

That failover only works if US-East can authenticate the users, read enough recent data, and run compatible service versions. Otherwise DNS failover simply moves users to a region that cannot serve them correctly.

---

## Storage Fundamentals

### Why Storage Matters

Storage concepts explain why:

- Databases use indexes
- Caches exist
- Kafka is fast
- SSDs outperform HDDs
- Systems try to avoid disk access

The key concept is:

```text
Sequential Access
vs
Random Access
```

---

## Sequential Access

Data is read in order.

Example:

```text
Block 1
Block 2
Block 3
Block 4
Block 5
```

Read pattern:

```text
1 → 2 → 3 → 4 → 5
```

Analogy:

Reading a book page by page.

---

### Where Sequential Access Appears

#### Video Streaming

```text
Second 1
Second 2
Second 3
Second 4
```

Continuous ordered reads.

---

#### Log Files

```text
Log 1
Log 2
Log 3
Log 4
```

New entries are appended.

Mostly sequential writes.

---

#### Kafka

Messages are appended to the end of a log.

```text
Message 1
Message 2
Message 3
Message 4
```

Sequential writes are a major reason Kafka achieves high throughput.

---

## Random Access

Data is accessed from arbitrary locations.

Example:

```text
Read Block 1
Read Block 8000
Read Block 27
Read Block 50000
Read Block 130
```

Analogy:

Reading:

```text
Page 1
Page 400
Page 13
Page 900
```

in a book.

---

### Where Random Access Appears

#### User Lookups

```sql
SELECT * FROM users WHERE id = 8273637;
```

The database jumps directly to the desired location.

---

#### Banking Systems

```text
Account 123
Account 999999
Account 45
Account 80000
```

Every lookup is unrelated to the previous one.

---

#### Social Media

```text
User A profile
User B profile
User C profile
```

The database constantly accesses different records.

---

## Why HDDs Hate Random Access

An HDD contains:

- Rotating platters
- Mechanical read/write heads

Each random read requires:

```text
Seek Time
+
Rotational Delay
+
Read Time
```

The expensive parts are:

- Moving the head
- Waiting for rotation

Example:

```text
Read 1 MB sequentially
```

might take:

```text
5 ms
```

while:

```text
1000 random reads
totaling the same 1 MB
```

might take:

```text
5000 ms
```

The amount of data is identical.

The access pattern is different.

---

## SSDs Improve Random Access

SSDs eliminate:

- Mechanical movement
- Rotational delay

Therefore:

```text
Random access becomes much faster
```

However:

```text
Sequential access is still preferred
```

because fewer I/O operations are needed.

The gap is smaller than HDDs, but still exists.

---

## Why Databases Care About This

### Full Table Scan

```sql
SELECT * FROM users;
```

The database can read pages sequentially.

Efficient.

---

### Point Lookup

```sql
SELECT * FROM users
WHERE id = 8273637;
```

The database performs targeted access.

This is random access.

---

## Why Indexes Exist

Without an index:

```text
1
2
3
4
...
10000000
```

The database scans many records.

With an index:

```text
8273637 → Page X
```

The database jumps directly to the desired location.

Indexes reduce the number of reads dramatically.

---

## Why Caches Are So Powerful

Latency hierarchy:

```text
CPU Cache
↓
RAM
↓
SSD
↓
HDD
↓
Network
```

Random SSD access is much slower than RAM access.

Instead of:

```text
Random SSD read
```

a cache provides:

```text
In-memory lookup
```

This is why:

- Redis
- Database buffer pools
- Application caches

can produce massive performance improvements.

---

### Core Mental Model

```text
Random disk access is expensive
        ↓
Databases use indexes
        ↓
Databases keep hot pages in RAM
        ↓
Applications add Redis caches
        ↓
Systems avoid disk whenever possible
```

This is the main interview takeaway from storage fundamentals.

---

## Gotchas / Trick Questions

### Gotcha #1: Does Horizontal Scaling Automatically Mean Redundancy?

Initial intuition:

> Redundancy only exists with failover servers or replicas.

Correction:

Horizontal scaling only creates redundancy when multiple nodes can perform the same function.

Example:

```text
LB
├─ S1
├─ S2
└─ S3
```

Each server can replace another server.

Redundancy exists.

---

### Gotcha #2: Does Sharding Automatically Create Redundancy?

Initial intuition:

> Adding more machines should increase redundancy.

Correction:

Sharding increases capacity, not redundancy.

Example:

```text
Shard A → Users 1-1000
Shard B → Users 1001-2000
Shard C → Users 2001-3000
```

If Shard B dies:

```text
Users 1001-2000 unavailable
```

No other shard can replace it.

No redundancy exists.

---

### Gotcha #3: If All App Servers Are Actively Used, How Can They Be Redundant?

Initial intuition:

> They're all serving traffic, so none are backups.

Correction:

Redundancy does not require idle components.

Redundancy means:

> Another component can perform the same function if one disappears.

This is active redundancy.

---

### Gotcha #4: Is Sequential Access Always Better Than Random Access?

No.

Random access is required for many workloads:

- User lookups
- Banking systems
- Social networks

The goal is not to eliminate random access.

The goal is to reduce expensive disk-based random access using:

- Indexes
- RAM
- Caches

---

## Performance Characteristics

| Operation | Typical Pattern |
|------------|------------|
| Video streaming | Sequential read |
| Log writing | Sequential write |
| Kafka append | Sequential write |
| User lookup by ID | Random read |
| Banking account lookup | Random read |
| Social profile lookup | Random read |
| Full table scan | Sequential read |
| Indexed lookup | Small number of random reads |

---

## Good to Know

### Why Kafka Is Fast

Kafka largely performs sequential appends to disk:

```text
Message 1
Message 2
Message 3
Message 4
```

Sequential disk operations are highly efficient.

---

### Why Redis Feels So Fast

Redis avoids disk access entirely for normal operations.

Instead of:

```text
Random SSD read
```

it performs:

```text
In-memory lookup
```

which is orders of magnitude faster.

---

## Quick Recall

**Q: What is vertical scaling?**  
A: Increasing CPU, RAM, or storage of a single machine.

**Q: What is horizontal scaling?**  
A: Adding more machines and distributing load across them.

**Q: Does horizontal scaling always create redundancy?**  
A: No. Only if multiple nodes can perform the same function.

**Q: Does sharding provide redundancy?**  
A: No. Sharding provides capacity. Replication provides redundancy.

**Q: What is active redundancy?**  
A: Multiple active components serving traffic while also being able to replace each other upon failure.

**Q: Why do stateless app servers scale better?**  
A: Any server can handle any request, so load balancers and autoscaling can add/remove instances without session pinning.

**Q: What are the hard parts of multi-region?**  
A: Routing users correctly, synchronizing data, handling regional cache misses, and keeping deployments consistent.

**Q: Why are sequential disk operations faster than random ones?**  
A: They avoid repeated seek and positioning overhead, especially on HDDs.

**Q: Why do caches improve performance so dramatically?**  
A: They replace expensive disk reads with much faster memory lookups.
