# CQRS (Command Query Responsibility Segregation)

In traditional CRUD, a single database model handles both reads (`SELECT`) and writes (`INSERT/UPDATE`). CQRS is the architectural pattern of splitting your system into two distinct halves:
1. **Commands:** Intentions to change state (`CreateOrder`, `UpdateInventory`).
2. **Queries:** Requests for information that do *not* change state (`GetOrderHistory`).

## Why CQRS?
Reads and writes have opposing optimization requirements. 
- **Writes** need validation, ACID transactions, locks, and strict business rules. 
- **Reads** need fast lookups, caching, search indexes, and heavily denormalized data (no JOINs).

By splitting them, you can scale and optimize each side completely independently.

## The 3 Levels of CQRS

> **Misconception:** *CQRS means copying data from PostgreSQL into Elasticsearch or Redis for reads. If you're just using views in one DB, that's not real CQRS.*
> 
> **Correction:** All of the following are valid forms of CQRS. CQRS only mandates that the read and write *models* are separate; it does not dictate the physical infrastructure.

1. **Level 1 (Separate Code Paths):** Same database, same tables. The code just uses two different models/handlers (e.g., `CreateUserCommandHandler` vs `GetUserQueryHandler`).
2. **Level 2 (Separate Read Models):** Same database, but reads hit specialized structures. The writes go to `Users` and `Orders` tables. The reads hit a `CustomerSummary` **Materialized View** that precomputes the JOINs.
3. **Level 3 (Separate Read Storage):** Different databases entirely. Writes go to PostgreSQL. An event is published, and the data is synced to Elasticsearch (for search), Redis (for fast profile lookups), and BigQuery (for marketing analytics). 

## CQRS + Event Sourcing

CQRS and Event Sourcing do not require each other, but they are the peanut butter and jelly of system design. 

In this combo, a **Command** is validated and saved as an event in the **Event Store** (the write database). That event (`EmailChanged`) is then published. A Projection Service consumes that event and updates the **Read Database** (e.g., an Elasticsearch index). The **Query** hits the Read Database.

## The Trade-offs

**Advantages:**
- **Independent Scaling:** If you have 1,000 writes/sec and 100,000 reads/sec, you can provision 2 write servers and 50 read servers.
- **Business-Oriented Commands:** `WithdrawMoney` captures business intent much better than `UPDATE balance = balance - 100`.
- **Flexible Read Models:** One write model can generate endless specialized read models (Admin views, Customer views, Marketing dashboards).

**Disadvantages (The Costs):**
- **Eventual Consistency:** When a user updates their profile (Write DB), there is a delay (ms to seconds) before the event propagates and updates the Read DB. If they immediately refresh the page, they might see their old data.
- **Duplicate/Out-of-Order Messages:** The infrastructure syncing the write DB to the read DB might deliver a message twice. The read projections must be **idempotent**.
- **Massive Complexity:** You double your infrastructure and conceptual overhead.

## CQRS vs Event-Driven Architecture (EDA)

> **Gotcha:** If a marketing team consumes events to update their analytics DB, is that CQRS or EDA?
> 
> It's both. **CQRS** is the architectural decision to structure reads separately from writes. **EDA** is the transport mechanism (publishing/consuming events) used to physically move the data from the write model to the read models.

## SQL View vs Materialized View (in CQRS)

In Level 2 CQRS, read models are often implemented as materialized views rather than regular SQL views.

- **SQL View:** A saved query. When queried, it reruns the underlying `JOIN`s on the fly. It takes no extra storage and is always up-to-date, but can be slow for complex read models.
- **Materialized View:** Stores the *results* of the query physically. When queried, it reads the precomputed rows without running `JOIN`s. It requires a refresh strategy (e.g., nightly batch or triggered by events) and uses extra storage, but provides massive read performance gains—exactly what CQRS aims for.

## Populating Read Models at Scale (Protecting the Primary DB)

When scaling to Level 3 (Separate Read Storage), copying data from the primary database to read models (like Elasticsearch or Redis) naively via heavy `SELECT` queries will crush the production database. 

Real systems use these approaches to move data without adding load to the primary DB:
1. **Read Replicas:** The simplest approach. Writes hit the primary DB, and read-heavy analytics or jobs hit a read replica. 
2. **CDC (Change Data Capture):** The modern scalable standard. Tools like Debezium read the primary database's transaction logs (e.g., PostgreSQL WAL) and stream only the incremental changes (INSERTs/UPDATEs) into a message broker like Kafka. Consumers then update Elasticsearch, Redis, or Snowflake. No huge table scans are needed.
3. **Event Sourcing:** If the system is already event-sourced, the primary "write" is publishing an event (e.g., `OrderPlaced`). Read models simply consume these events directly from the event broker to build their views, completely bypassing any database reads.
4. **Batch ETL:** An older approach where a nightly cron job copies only rows updated since the last run (incremental changes) to a data warehouse.

*These downstream databases are essentially read models optimized for a particular use case (Elasticsearch for search, Redis for low-latency lookups, ClickHouse for analytics).*

## Where CQRS Shines

- **Common in:** Banking systems, trading platforms, e-commerce, logistics, event-sourced systems, and large microservice architectures.
- **Overkill for:** Simple CRUD applications, small internal tools, admin dashboards, or simple monoliths.

## Quick recall

**Q. Why not just use a standard SQL View instead of a Materialized View for a read model?**
A. A standard SQL View reruns the underlying joins every time it is queried, which can be slow. A Materialized View stores the precomputed results physically, providing fast, single-table lookups at the cost of eventual consistency.

**Q. If you need to populate an Elasticsearch read model from a PostgreSQL write database, how do you do it without overloading PostgreSQL?**
A. Use Change Data Capture (CDC) like Debezium to tail the PostgreSQL transaction log (WAL) and stream changes to Kafka, which Elasticsearch then consumes.
