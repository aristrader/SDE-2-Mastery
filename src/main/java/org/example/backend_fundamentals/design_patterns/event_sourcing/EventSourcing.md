# Event Sourcing & Event-Driven Architecture

Instead of storing only the current state of data (e.g. `Balance = 1200`), **Event Sourcing (ES)** stores the full sequence of actions that occurred (`AccountCreated(1000) → Deposit(500) → Withdraw(300)`). The append-only event log is the absolute source of truth. Current state is reconstructed by **replaying** the events.

## Event Sourcing vs. Event-Driven Architecture (EDA)

This is one of the most common interview confusions. They sound similar but solve completely different problems.

| Feature | Event Sourcing | Event-Driven Architecture |
|----------|----------|----------|
| **Primary Goal** | **Store data** (Persistence) | **Communicate data** (Integration) |
| **Role of Events** | Events *are* the database | Events are messages moving between services |
| **Stores current state?** | No, state is derived by replaying events | Usually yes, services keep their own standard DBs |

**The Relationship:** 
You can use ES inside an EDA. Many enterprise systems combine both beautifully:
1. **Event Sourcing (Persistence):** A request hits the Transaction Service, which appends the event to the **Ledger Store** (the source of truth).
2. **EDA (Communication):** The service then publishes that same event to **Kafka** (the transportation layer).
3. **Consumers:** Downstream services (Fraud, Analytics, Notifications) consume from Kafka. 

However, **EDA does not require Event Sourcing**. The vast majority of companies just use a standard PostgreSQL database, update a row, and then publish an event to Kafka (EDA without ES).

## The Banking Example: How it actually works

Banks don't use pure Event Sourcing everywhere, but their core **Ledger** works almost identically: an append-only transaction history.

> **Misconception:** *If everything is stored as a log of events, then checking my balance requires the system to replay 10 years of transactions every single time I open the app.*
> 
> **Correction:** This would be prohibitively slow. Real systems maintain **both**:
> 1. **The Ledger (Event Store):** The immutable source of truth containing all events.
> 2. **The Balance Table (Read Model/Materialized View):** The current state (`Balance = 1400`). Think of this as a cache (like Redis is to MySQL).
> 
> When a deposit happens: (1) Append to Ledger. (2) Update Balance Table. When a read happens, it hits the Balance Table. If the Balance Table is ever corrupted, you wipe it and rebuild it by replaying the Ledger.

**What about millions of events?**
To scale, systems partition/shard ledgers (e.g., by AccountID). To avoid replaying from the beginning of time during a rebuild, systems take periodic **Snapshots** (e.g., `Balance = 450,000 at Event #1,000,000`). To rebuild, you load the snapshot and only replay events that occurred *after* it.

> **Misconception:** *Events must never be deleted.*
> 
> **Correction:** In *pure* academic Event Sourcing, you never delete. In the real world, storage grows infinitely. Old events are eventually compressed, archived to cold storage (after 7+ years), and purged according to legal data-retention policies.

## Why use Event Sourcing? (The "Disaster Recovery" Myth)

A very common but weak explanation for Event Sourcing is *"if the database dies, we can rebuild it from the event log."* In reality, modern databases have replicas, backups, and Point-In-Time Recovery (PITR). Rebuilding from an event log for pure disaster recovery is rarely the primary driver. 

The *real* reasons are:
- **The Legal Truth:** The ledger explains *why* the balance is what it is. A balance is merely derived state (`Sum(credits) - Sum(debits)`). The ledger is the unassailable truth used for fraud investigation and regulatory audits.
- **The Git Analogy:** A bank ledger is basically Git commit history for money. A traditional DB only stores the current files without history, branches, or `git blame`.
- **Perfect Audit Trail:** You have the complete history of how a system reached a state (crucial for Banking, Healthcare, Compliance).
- **Time Travel & Historical Reconstruction:** "What was the user's balance on Jan 5th?" Traditional databases can't answer this easily. With ES, just replay events up to Jan 5th.
- **Bug Fixes:** If an interest calculation was wrong for 3 days, you can fix the calculation logic, replay the affected events, and regenerate the correct balances.
- **Natural fit for CQRS:** One event stream can generate multiple different read projections (Customer View, Admin View, Analytics View) without duplicating business logic.

## The Trade-offs (Why not to use it)

- **Extreme Complexity:** Instead of a simple `UPDATE`, you need to create events, store them, publish them, update projections, manage snapshots, and handle eventual consistency.
- **Event Versioning:** If you change an event's structure (`MoneyDeposited` now requires a `currency` field), you have to maintain backward compatibility for all historical events.
- **Overkill for CRUD:** Do not use this for basic admin dashboards or simple internal tools. Use it only when the *journey* of the data is as important as the *current state*.
