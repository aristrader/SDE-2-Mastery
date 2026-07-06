---
order: 20
---

# Clustering — Cooperating Nodes, Heartbeats, Leader Election

## How it works

### What a cluster is

A cluster is a group of machines that **cooperate and coordinate to behave as a single logical system**. The important idea is not "multiple servers exist" — it's that the servers are *aware of each other* and actively work together: they communicate, coordinate state, detect failures, and often elect leaders.

Examples: Redis Cluster, Kafka cluster, database clusters, Kubernetes control-plane clusters.

### Clustering vs load balancing — the classic trap

A load balancer simply **distributes traffic**; a cluster is about **cooperation between nodes**.

```text
Load balancing:                    Clustering:
Client → LB → [S1 | S2 | S3]       Node A ⇄ Node B ⇄ Node C
(servers may know nothing          (heartbeats, replication,
 about each other)                  failure detection, leader election)
```

The LB's job: spread traffic, improve throughput, prevent overload. The cluster itself provides functionality beyond serving traffic: exchanging heartbeats, replicating data, detecting failures, electing leaders, coordinating state.

### Redis Cluster example

Data is **sharded** across nodes — each shard stores only part of the dataset:

```text
Shard 1 → user IDs 1–1000
Shard 2 → user IDs 1001–2000
Shard 3 → user IDs 2001–3000
```

A request like `GET user:1500` is routed to the node that owns that shard.

**Replication inside the cluster** — sharding alone is not enough; each shard typically has replicas:

```text
Primary A → Replica A1
Primary B → Replica B1
Primary C → Replica C1
```

The primary handles writes; replicas copy data from the primary, can serve reads (configuration-dependent), and are failover candidates.

### Leader election and failover

If a primary fails, the cluster detects it and **promotes a replica**:

```text
Primary A (FAILED) → Replica A1 promoted → new Primary A
```

This is leader election — the goal is restoring availability without manual intervention.

### Heartbeats

Clusters determine whether nodes are alive via heartbeats ("I'm alive" messages). If heartbeats stop arriving from a node, the cluster assumes it failed and can trigger failover. Heartbeats are the foundational building block for **failure detection, failover, and leader election**.

### Kafka cluster example

A Kafka cluster contains multiple brokers. Topics split into partitions; each partition has **one leader broker and one or more followers**:

```text
Orders topic, Partition 0:
Leader   → Broker 1
Follower → Broker 2
Follower → Broker 3
```

Writes go to the leader; followers replicate from it. If the leader broker fails, a follower is promoted — leader election again.

### The common pattern

```text
Multiple nodes + heartbeats + replication + leader election + failover
```

Redis, Kafka, databases, and Kubernetes all implement variations of this same pattern. Implementations differ; the fundamental ideas remain the same.

## Gotchas / Trick questions

1. **"Multiple servers behind a load balancer means I have a cluster."** Not necessarily — if the servers don't coordinate state, replicate data, exchange heartbeats, or elect leaders, that's load balancing, not clustering.
2. **"Load balancing and clustering are basically the same."**

   | Load balancing | Clustering |
   |----------------|------------|
   | Distributes traffic | Coordinates nodes |
   | Focused on request routing | Focused on system cooperation |
   | Servers may be independent | Nodes actively communicate |
   | Improves throughput | Improves availability, coordination, state management |

3. **"Redis clustering is only about adding more machines."** No — it involves data sharding, replication, failure detection, replica promotion, and leader election.
4. **"Leader election is only a database concept."** It appears in Redis, Kafka, database clusters, and the Kubernetes control plane.
5. **"Heartbeats are just monitoring."** They're the core mechanism driving failure detection, failover decisions, and leader election — without them the cluster can't reliably know whether a node is alive.

## Performance characteristics

**Benefits:** higher availability, automatic failover, better fault tolerance, data distribution across nodes, scaling beyond a single machine.

**Trade-offs:** coordination overhead — nodes must communicate, replicate, exchange heartbeats, and manage leadership changes. The price buys availability and resilience.

## Good to know

All the clustered systems discussed — Redis Cluster, Kafka, database clusters, Kubernetes control plane — use the same broad ideas (cooperating nodes, heartbeats, replication, failover, leader election). The implementation details differ, but the mental model transfers between them.

## Quick recall

**Q. What is the primary purpose of clustering?**
A. Multiple machines cooperate and coordinate to behave as one logical system.

**Q. Load balancing vs clustering?**
A. Load balancing distributes traffic; clustering coordinates nodes.

**Q. Why is Redis Cluster a cluster and not just multiple Redis servers?**
A. Sharding, replication, failure detection, and failover coordination.

**Q. What is the role of a replica in Redis?**
A. It copies data from the primary and can be promoted during failover.

**Q. Why do clusters use heartbeats?**
A. To detect failed nodes and trigger failover or leader election.

**Q. How does Kafka use clustering?**
A. Each partition has a leader and followers; a follower is promoted if the leader fails.

**Q. What pattern is common across Redis, Kafka, DB clusters, and Kubernetes?**
A. Heartbeats + replication + leader election + failover among cooperating nodes.


