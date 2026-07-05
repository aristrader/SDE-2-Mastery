---
order: 20
---

# Database Federation

A federated database presents **multiple independent databases as one logical database**. A federation layer sits in front, receives a query, decides which database(s) to hit, collects results, and returns a unified response. The interview value is almost entirely the **federation-vs-sharding** contrast.

```text
Application → Federation Layer → { user_db, order_db, payment_db }
```

Instead of one `amazon_db` holding users/orders/products/payments, you split by **domain** into `user_db`, `order_db`, `catalog_db`, … and the federation layer makes them look like one system. "Show Swapnil's profile and last 5 orders" → the layer runs `SELECT … FROM user_db.users WHERE id=123` and `SELECT … FROM order_db.orders WHERE user_id=123`, then merges.

## Federation vs sharding (the interview favorite)

| | Federation | Sharding |
|---|---|---|
| **Purpose** | Separate business **domains** | Scale **one** large dataset |
| **Data** | Different datasets | Same dataset, split |
| **Schema** | Usually different per DB | Identical across shards |
| **Routing** | By business domain | By shard key |
| **Ownership** | Often different teams | Usually one team |
| **Tech** | Can mix (PG, Mongo, Cassandra) | Usually one engine |

One-liners: **federation** = many *different* databases pretending to be one; **sharding** = one *large* dataset cut into partitions for scale.

## Why federation

- **Team independence** — Payments team owns `payment_db`, Orders team owns `order_db`; less accidental interference.
- **Technology freedom** — each domain picks its own engine (Postgres / Mongo / Cassandra); the layer hides it.
- **Independent scaling** — if only Orders grows, scale `order_db` without touching the others.

## The two hard problems

- **Cross-database joins.** `SELECT … FROM users u JOIN orders o ON u.id=o.user_id` when `users` and `orders` live in different DBs forces the federation layer to query each DB and **do the join itself** — more complex and slower than a native DB join.
- **Distributed transactions.** "Charge card AND create order" spans `payment_db` + `order_db`, so you're back to **2PC or Saga** (`databases/distributed_transactions/DistributedTransactions.md`). The DB can't give you a single local ACID transaction across two engines.

## Federation layer vs application layer

A federation layer is essentially **specialized middleware** between apps and databases — query routing, result combining, cross-DB coordination, unified interface. Conceptually it's not fundamentally different from a custom service layer that talks to several databases; "federation" just names the case where its whole job is presenting many DBs as one.

## Modern reality — why it faded

True federated databases are uncommon in modern microservices. Instead of *many DBs hidden behind one federation layer*, systems use **database-per-service**:

```text
Order Service → order_db    Payment Service → payment_db    Inventory Service → inventory_db
```

Coordination moves **out of the database** to REST/gRPC, events, Kafka, and **Saga** workflows. Each service owns its DB completely — no cross-database joins, no central distributed-transaction coordinator. Federation faded because it gets complex, slow, and hard to maintain at scale.

**Misconception:** *Saga makes federation entirely unnecessary.*
**Correction:** mostly true for modern microservices — with database-per-service + Saga, each service handles its own DB and coordination is event-driven, so the centralized federation layer disappears. But *aggregation* doesn't vanish; it moves up a layer.

## The federation-like read layer survives: BFF

When a screen needs user profile + recent orders + payment status from three services, you don't want the frontend making three calls. A **Backend-For-Frontend (BFF)** calls the services, aggregates, and returns one payload:

```text
Frontend → BFF → { User Service, Order Service, Payment Service }
```

This *resembles* federation, but it aggregates at the **API/service layer**, not the **database layer** — and it talks to service APIs, not raw databases.

## Quick recall

**Q. Federation vs sharding?**
A. Federation = multiple *different* databases (by domain) presented as one logical DB. Sharding = one *large* dataset split into partitions by shard key for scale.

**Q. What are federation's two biggest drawbacks?**
A. Cross-database joins (the layer must join in application space) and distributed transactions across DBs (need 2PC/Saga).

**Q. Why did database federation fade in modern microservices?**
A. Database-per-service + event/Saga coordination replaced it; federation became complex, slow, and hard to maintain at scale.

**Q. Does Saga remove the need for aggregation entirely?**
A. It removes the centralized DB-level federation layer, but read-side aggregation reappears at the API layer as a BFF.

**Q. BFF vs federation?**
A. Same "combine many sources into one response" idea, but BFF aggregates at the service/API layer (calling service APIs), not at the database layer.


<ExerciseNav />
