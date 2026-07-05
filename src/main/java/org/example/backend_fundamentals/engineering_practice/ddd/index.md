---
order: 20
---

# Domain-Driven Design & Bounded Contexts

The strategic side of DDD — the part interviewers actually probe, and the part that drives service boundaries. (Tactical building blocks — Aggregate / Entity / Value Object — are a separate topic: Part 04 row 59.)

**Core idea:** software structure should mirror **business** structure. Organize around **business domains** (Customer Management, Orders, Payments, Inventory, Shipping) — *not* around database tables, controllers, packages, or technologies.

## Bounded Context — the key concept

A **Bounded Context** is a **business/domain boundary**, *not* a database boundary. Inside a context, a term has one clear, owned meaning.

**Misconception:** *bounded context = separate database.*
**Correction:** it's about **ownership and meaning**, not storage. Multiple contexts can read the same physical DB and still be distinct contexts; conversely, splitting databases is a *later, optional* implementation choice. DDD does **not** require 50 databases — it requires clearly defined ownership.

## Why it matters — the "one big customer table" decay

Watch a shared `customers` table rot as teams pile on (the classic motivation for DDD):

```text
Stage 1 (startup):   customers(id, name, email, phone, address)        -- simple, 2 devs
Stage 3 (marketing): + favorite_category, last_campaign_clicked, marketing_score
Stage 4 (support):   + total_complaints, vip_customer, last_ticket_status
Stage 5 (payment):   + billing_address, tax_id, fraud_score
→ 30+ columns, nobody knows ownership or what's safe to change
```

**The real pain:** Marketing drops `marketing_score` → Support, Payment, and Analytics all break, because everyone silently depended on it. The database has stopped modeling *the business* and started modeling *everybody's mixed requirements*.

## There is no single universal "Customer"

The same customer "Swapnil" means different things to different teams:

- **Marketing** — favorite categories, shopping frequency, ad preferences, segments.
- **Support** — complaint history, refunds, open tickets, escalations.
- **Shipping** — delivery address, courier preferences, delivery instructions.
- **Payment** — cards, UPI IDs, billing details, fraud score.

**Big insight:** stop asking *"what is a Customer?"* Ask *"what is a Customer inside Marketing? inside Shipping? inside Payments?"* Each answer is a bounded context that **owns** its slice (its meaning, its rules, its data).

## Ownership first, separate databases later

Defining contexts (Marketing owns preferences/campaigns/segments; Support owns tickets/complaints; Payment owns cards/invoices/refunds) can happen while **still on one database** — the win is clear ownership. Only later, at large scale (say 500 devs), does the shared DB become painful enough to split into `marketing_db`, `payment_db`, `support_db`.

**Data duplication is then normal and acceptable.** Each context keeps `customer_id` (and a local copy of the few customer fields it needs):

```text
marketing_db: customer_id, favorite_category, campaign_score
support_db:   customer_id, ticket_count, last_ticket
payment_db:   customer_id, upi_id, billing_address
```

**Why duplicate instead of always calling Customer Service?** Because a hard dependency means *if Customer Service is down, Marketing is down*. A small local copy keeps Marketing **independent**. Modern distributed systems prefer **small duplication over heavy dependency**.

## DDD → microservice boundaries

This is why people say *"DDD helps identify microservice boundaries."* Once you've discovered where business responsibilities naturally begin and end — Payment Context, Order Context, Inventory Context, Shipping Context — those bounded contexts are the natural seams to later split into Payment Service, Order Service, etc. DDD gives you *correct* boundaries before you pay the microservices tax (`system_design/architecture/MonolithsVsMicroservices.md`).

**DDD is NOT about** databases, Kafka, or microservices. **DDD IS about** discovering where business responsibilities naturally begin and end.

## Quick recall

**Q. What is a bounded context?**
A. A business/domain boundary within which a term has one owned meaning — about ownership and meaning, *not* a database boundary.

**Q. Does DDD require separate databases per context?**
A. No. Contexts define ownership; you can stay on one database. Splitting databases is a later, optional scaling choice.

**Q. What's the core motivation for bounded contexts?**
A. A shared "god" table accumulates every team's fields until ownership is lost and one team's change breaks others — because "Customer" means different things to Marketing, Support, Shipping, and Payment.

**Q. Why is data duplication across contexts acceptable?**
A. A local copy keeps a context independent; a hard cross-service call couples availability (Customer Service down ⇒ Marketing down). Prefer small duplication over heavy dependency.

**Q. How does DDD relate to microservices?**
A. Bounded contexts reveal the natural business seams; those seams become microservice boundaries — so DDD gives correct boundaries before you split.

**Q. What is DDD fundamentally about?**
A. Structuring software around business responsibilities (domains) rather than tables/controllers/tech — discovering where responsibilities begin and end.
