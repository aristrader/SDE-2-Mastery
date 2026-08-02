---
order: 10
---

# SQL vs NoSQL — Schemas, Transactions, Relationships, Sharding

## Database-level security — defense in depth

Most day-to-day authorization happens in the **application layer**:

```java
if (!user.isAdmin()) {
    throw new ForbiddenException();
}
deleteUser(id);
```

The database never sees unauthorized requests. So is database security still important? Yes — security is enforced at **multiple layers**. The database also controls who can connect / read / write / delete:

```sql
GRANT SELECT ON users TO analyst;   -- analyst can read but not modify
```

Real production setups use different DB users with different permissions: application user, analytics user, read-replica user, DBA user.

**Why it matters — defense in depth:** if an application bug or SQL injection occurs and the compromised DB user only has `SELECT`, the attacker still cannot `DROP TABLE users` — the database blocks it.

**Interview answer:** authorization is usually enforced in the application layer, but databases provide additional role-based access controls for operational safety and defense in depth.

## Schemas and "schemaless"

**Misconception:** *MongoDB = no schema; "schemaless" = same as "flexible schema"?*

**Correction:** every database has a schema in some form — the difference is **strict vs flexible**.

- SQL: `CREATE TABLE users(age INT)` — only integers allowed in `age`.
- MongoDB: `{"name":"Alice"}` and `{"name":"Bob","age":25}` are both valid documents in the same collection.

"Schema-less" usually means *schema-flexible*, not *no schema exists*. And modern MongoDB can even enforce validation (name must be string, age must be integer, email required) — modern NoSQL can behave much closer to SQL than many people realize.

## Why SQL became popular

Structured data, consistency, transactions, querying power, data integrity.

## ACID and transactions — not SQL-only

**Misconception:** *SQL ⇒ transactions; NoSQL ⇒ no transactions.*

**Correction:** transactions are a **database feature**, not a SQL-only feature.

PostgreSQL:

```sql
BEGIN;
UPDATE A;   -- debit
UPDATE B;   -- credit
COMMIT;     -- crash before commit → ROLLBACK
```

Modern MongoDB:

```javascript
session.startTransaction()
debit(A)
credit(B)
session.commitTransaction()   // rollback also possible
```

## Foreign keys and referential integrity

**Referential integrity:** every reference must point to something that actually exists. An `orders` row with `user_id = 999` when no user 999 exists is an **orphaned record**. A foreign key makes the database prevent it:

```sql
FOREIGN KEY(user_id) REFERENCES users(id)
```

### "If big companies remove foreign keys, doesn't consistency suffer?"

Yes, potentially — a trade-off exists.

- **Small company:** users and orders in one database → the database enforces the relationship.
- **Large company:** User Service → DB A, Order Service → DB B. **Foreign keys cannot span databases** — the application must enforce the relationship.

| Database enforcement | Application enforcement |
|----------------------|-------------------------|
| More integrity | More flexibility |
| Less flexibility | More responsibility |

**The important distinction — two separate questions:**

1. *Should relationships exist?* → Yes, always.
2. *Who enforces them?* → Database **or** application. Big companies don't remove relationships — they move enforcement from the database to the application.

## Why SQL is still used (the banking question)

**Misconception:** *SQL is used in banking because NoSQL lacks transactions.* Outdated — modern MongoDB supports transactions.

**Real reason: banking data is highly relational.** Entities: customer, account, transaction, loan, card, audit record. Customer → accounts → transactions; customer → loans; customer → cards.

- **SQL representation:** customers / accounts / transactions tables — relationships explicit.
- **MongoDB representation:** initially nest everything (`customer → accounts → transactions[…]`) — looks nice. Problem: millions of transactions → the document becomes huge → eventually you split into `customers` / `accounts` / `transactions` collections — **and relational relationships exist again**.

**Core insight:** many business systems are inherently relational *regardless of database technology*.

- SQL strength: relationships, joins, constraints, referential integrity.
- MongoDB strength: document access patterns, flexible schemas.

**Better interview answer:** don't say "MongoDB can't do banking" (wrong) or "SQL is used because MongoDB lacks transactions" (outdated). Say: *banking systems are highly relational; SQL databases provide first-class support for relationships, joins, constraints, referential integrity, and mature transactional semantics. NoSQL can be used, but more responsibility shifts to application code.*

## Two meanings of "consistency"

**Confusion:** *if MongoDB has transactions, SQL consistency and NoSQL consistency seem the same.*

"Consistency" is used in two different ways:

1. **ACID consistency** — e.g. "balance cannot become negative." MongoDB can support this.
2. **Data integrity** — e.g. "every order belongs to a valid user." Relational databases provide stronger *built-in* support via constraints and foreign keys.

## Data modeling: normalize vs denormalize

- **SQL: normalize.** Users / orders / products / payments in separate tables, relationships through joins, avoid duplication. Motto: *avoid duplication, maintain consistency.*
- **NoSQL: denormalize.** Store related data together (`{"user":"Alice","orders":[…]}`), duplicate data if necessary, avoid joins. Motto: *duplicate data, optimize access patterns.*

## Design mindset: entity-first vs access-pattern-first

Beyond normalize/denormalize, the two technologies push a different *starting point* for design — this is the framing interviewers want.

- **SQL mindset — entity-first / relationship-first.** Start from the entities (users, orders, products, payments), model the relationships between them, normalize, and let flexible querying (ad-hoc joins) come later. You design the data, then query it however you need.
- **NoSQL mindset — access-pattern-first.** Start from *"what queries will I run most often?"* (e.g. "get user profile with recent orders"), then shape storage around those queries. You design for the read, and pay for new access patterns with new denormalized copies.

**Interview framing:** SQL lets you defer query decisions because joins are cheap and flexible; NoSQL forces you to know your access patterns up front because the storage layout *is* the query plan.

## Scale alone doesn't dictate NoSQL

**Misconception:** *large scale ⇒ NoSQL.*

**Correction:** plenty of very large systems run PostgreSQL / MySQL at massive scale. The choice is driven by **data model, query patterns, operational requirements, and team expertise** — not raw user count. "We have a lot of users" is not, by itself, a reason to reach for NoSQL.

Note the symmetry with the transactions point above: **SQL has gotten better at scaling** (partitioning, replicas, managed sharding) and **NoSQL has gotten better at transactions**. So the old "SQL = transactions, NoSQL = scale" split no longer drives the decision — *data shape and access patterns* do.

## When NoSQL is actually a good fit

NoSQL is strongest when the application does not need relational joins as the main access path:

| Need | Common NoSQL fit | Why |
|------|------------------|-----|
| Very fast lookup by key | Key-value store | Simple `key → value` access, easy horizontal partitioning |
| Flexible JSON-like records | Document store | Store an aggregate close to how the app reads it |
| Huge write-heavy event/time-series data | Wide-column / log-style store | Designed for high write volume and partitioned storage |
| Relationship traversal | Graph database | First-class edges and graph queries |

The decision question is: **can I answer my important queries without joins and without rebuilding relational integrity in application code?** If yes, NoSQL may simplify the hot path. If no, SQL is usually the cleaner starting point.

## NoSQL scaling

*"When people say NoSQL scales better, does it mean sharding is built in?"* — mostly yes. Many NoSQL systems were designed around horizontal scaling from day one: MongoDB → built-in sharding; Cassandra → built-in partitioning; DynamoDB → automatic partitioning.

## Sharding

Split data across multiple servers:

```text
Shard 1 → users 1–25M     Shard 3 → users 50M–75M
Shard 2 → users 25M–50M   Shard 4 → users 75M–100M
```

### When to shard

Bad answer: *"when data is large."* Better: **when a single database becomes a bottleneck** — storage limits, CPU limits, memory limits, read/write throughput limits, future growth limits.

### When NOT to shard

A 20 GB database at 500 req/s — Postgres handles it. Sharding introduces cross-shard joins, cross-shard transactions, rebalancing, and operational complexity.

**Interview answer:** shard only after query optimization, indexing, caching, read replicas, and vertical scaling have been exhausted.

## Gotchas / Trick questions

1. **"NoSQL has no schema."** It has a *flexible* schema, not an absent one — and modern NoSQL can enforce validation.
2. **"Transactions are SQL-only."** Modern NoSQL (MongoDB sessions) supports multi-document transactions with rollback.
3. **"SQL is used in banking because NoSQL lacks transactions."** Outdated. The real reason: banking is highly relational, and SQL models relationships/constraints/integrity natively.
4. **"Big companies remove relationships."** Relationships still exist — *enforcement* moves from the database (FKs can't span service databases) to the application.
5. **"NoSQL consistency = SQL consistency now."** Distinguish ACID consistency (both can do) from built-in data-integrity enforcement (relational constraints/FKs are stronger).
6. **"Data is big, let's shard."** Shard when a single DB is the *bottleneck*, and only after optimization/indexing/caching/replicas/vertical scaling are exhausted.
7. **"Large scale means NoSQL."** No — large systems run on Postgres/MySQL too. The driver is data model + query patterns + operational needs + team expertise, not user count.
8. **"NoSQL means no relationships."** Wrong — relationships still exist. NoSQL often makes the application duplicate, embed, or maintain relationship links manually.

## Quick recall

**Q. What does "schemaless" actually mean?**
A. Schema-flexible — every database has a schema in some form; NoSQL just doesn't enforce it strictly (though it can, via validation).

**Q. Are transactions SQL-only?**
A. No — they're a database feature. Modern MongoDB supports multi-document transactions with rollback.

**Q. Why is SQL still preferred for banking?**
A. Banking data is highly relational; SQL gives first-class relationships, joins, constraints, referential integrity, and mature transactional semantics.

**Q. What happens to foreign keys in a microservices world?**
A. FKs can't span databases — relationships still exist, but enforcement moves to the application (flexibility ↑, responsibility ↑).

**Q. Two meanings of "consistency"?**
A. ACID consistency (invariants like non-negative balance) vs data integrity (valid references) — relational DBs have stronger built-in support for the latter.

**Q. SQL vs NoSQL modeling philosophy?**
A. SQL: normalize, avoid duplication, join. NoSQL: denormalize, duplicate freely, optimize for access patterns.

**Q. When do you shard?**
A. When a single database is the bottleneck — and only after query optimization, indexing, caching, read replicas, and vertical scaling are exhausted.

**Q. SQL vs NoSQL design mindset?**
A. SQL is entity-first/relationship-first (model entities, normalize, query flexibly later); NoSQL is access-pattern-first (design storage around the queries you'll run most).

**Q. Does large scale force NoSQL?**
A. No — Postgres/MySQL run at massive scale. The decision is data model, query patterns, operational needs, and team expertise, not user count.

**Q. When is NoSQL a good fit?**
A. When the hot access pattern is key/document/partition based and does not depend heavily on joins or database-enforced relationships.
