---
order: 50
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

#### Stage 4

```text
Apps
 |
Cache
 |
DB
```

#### Stage 5

```text
Shard 1
Shard 2
Shard 3
Shard 4
```

This progression is essentially the story of scaling a system.

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

**Q: Why are sequential disk operations faster than random ones?**  
A: They avoid repeated seek and positioning overhead, especially on HDDs.

**Q: Why do caches improve performance so dramatically?**  
A: They replace expensive disk reads with much faster memory lookups.


