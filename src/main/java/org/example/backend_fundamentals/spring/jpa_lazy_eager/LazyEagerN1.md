# Lazy vs Eager Loading & N+1

---

## FetchType defaults

| Association | Default FetchType | Rationale |
|---|---|---|
| `@OneToMany` | `LAZY` | Could be a large collection — load on demand |
| `@ManyToMany` | `LAZY` | Same — potentially unbounded |
| `@ManyToOne` | `EAGER` | Usually a single related entity, small cost |
| `@OneToOne` | `EAGER` | Single entity, historically assumed cheap |

The defaults are not always right. `@ManyToOne(fetch = EAGER)` inside an entity used in a `@OneToMany` list creates a Cartesian product trap (covered below). Always make the fetch type explicit.

---

## LAZY loading mechanics

With `FetchType.LAZY`, Hibernate creates a **proxy object** for the association — looks like the real object but the database has not been queried. SQL fires the first time you call any method on the proxy (any getter, `toString`, etc.).

```java
Order order = orderRepository.findById(id).orElseThrow();
// 1 SELECT: SELECT * FROM orders WHERE id = ?

List<OrderItem> items = order.getItems();  // still no SQL — proxy returned
items.size();                              // NOW the SQL fires
// SELECT * FROM order_items WHERE order_id = ?
```

**`LazyInitializationException`:** thrown when a lazy association is accessed after the Hibernate session is closed. Common in controllers that receive an entity from a service — the `@Transactional` boundary ended in the service layer, so the session is gone by the time the view or serializer touches the association.

**Fixes:**
- Keep the access inside the `@Transactional` boundary (extend the transaction to the caller, or fetch eagerly in the query).
- Use a DTO projection — load only the fields you need inside the transaction; the view gets a plain object with no proxies.
- **Open Session in View (OSIV):** Spring Boot enables this by default (`spring.jpa.open-in-view=true`). It binds a Hibernate session to the HTTP request thread for the entire request, so lazy loading works in the controller and view layer. Eliminates `LazyInitializationException` but is an anti-pattern: leaks DB connections into the presentation layer, hides N+1 queries, and keeps connections open far longer than needed. Disable it (`spring.jpa.open-in-view=false`) in production and fix lazy access explicitly.

---

## The N+1 problem

Load N root entities → access a lazy association on each → N additional queries. Total: N+1.

```java
// Load 100 orders — 1 query
List<Order> orders = orderRepository.findAll();

for (Order order : orders) {
    // Hibernate fires 1 SELECT per order to load items
    // 100 orders = 100 extra queries
    process(order.getItems());
}
// Total: 101 queries
```

Silent — the code works correctly, just with catastrophic query volume. At 1000 orders: 1001 queries. At 10 000: 10 001 queries.

**Why it actually hurts:** the cost isn't query execution itself — it's the network round trips, connection usage, query parsing, and repeated work per query. **Root cause:** the application doesn't know its full data-access pattern ahead of time, so it keeps asking the database for more information one entity at a time.

### How to diagnose

Enable Hibernate SQL logging and statistics:

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
```

Or add `p6spy` / Datasource-proxy to count queries per HTTP request. The smell: 1 query for the list, then many identical queries differing only in the `WHERE` value.

---

## Fix 1 — JOIN FETCH

Write JPQL with `JOIN FETCH` to load the association in the same SQL join:

```java
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
List<Order> findByStatusWithItems(@Param("status") String status);
```

Hibernate generates one SQL `JOIN`, loading orders and items in a single roundtrip. Most direct fix, with full control over when the join happens.

Limitation: you can only `JOIN FETCH` one collection per query without hitting the Cartesian product problem (see below). For two collections, use separate queries or `@EntityGraph`.

---

## Fix 2 — @EntityGraph

Specify which associations to eagerly fetch at the repository method level without writing JPQL:

```java
@EntityGraph(attributePaths = {"items", "customer"})
List<Order> findByStatus(String status);
```

Hibernate generates a `LEFT JOIN FETCH` for each path. Cleaner than duplicating `JOIN FETCH` across queries. Equivalent under the hood — same Cartesian product risk with multiple collections.

Named entity graphs (declared with `@NamedEntityGraph`) allow reuse across repository methods via `@EntityGraph(value = "Order.withItems")`.

---

## Fix 3 — @BatchSize

Rather than a join, tell Hibernate to load the lazy collection using an `IN` clause when it eventually fires:

```java
@Entity
public class Order {
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<OrderItem> items;
}
```

When any 50 orders' `items` are accessed, Hibernate fires one query:

```sql
SELECT * FROM order_items WHERE order_id IN (?, ?, ..., ?)  -- up to 50 IDs
```

For 100 orders: 1 (orders) + 2 (items in 2 batches of 50) = 3 queries instead of 101. Not as good as `JOIN FETCH` but requires no query changes — good for existing code where you can't easily change every query.

---

## Fix 4 — DTO projections

Avoid loading entities and their associations entirely — project only the fields you need:

```java
// JPQL constructor expression
@Query("SELECT new com.example.OrderSummary(o.id, o.status, COUNT(i)) " +
       "FROM Order o LEFT JOIN o.items i GROUP BY o.id, o.status")
List<OrderSummary> findOrderSummaries();
```

Or use interface projections:

```java
public interface OrderSummary {
    Long getId();
    String getStatus();
    String getCustomerName();  // from a joined customer
}

List<OrderSummary> findByStatus(String status);  // Spring Data generates the projection query
```

No entity lifecycle, no proxy objects, no lazy initialization risk. Fastest option for a read-only view. Trade-off: projections don't support writes — you still need full entities to update state.

---

## Cartesian product problem

`JOIN FETCH` on two `@OneToMany` collections in a single query produces a Cartesian product:

```
Order has 3 items and 2 payments
JOIN FETCH items AND payments = 3 × 2 = 6 rows for this one order
Hibernate de-duplicates but the result set is bloated
```

Symptoms: duplicate parent entities in the result list (before deduplication), huge result sets, memory pressure.

**Fix with `DISTINCT`:**

```java
@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
```

`DISTINCT` in JPQL de-duplicates at the Hibernate level (Hibernate filters duplicates in memory, not just SQL). Use `QueryHints` with `PASS_DISTINCT_THROUGH = false` to suppress the `DISTINCT` from the SQL when unnecessary:

```java
@QueryHints(@QueryHint(name = HINT_PASS_DISTINCT_THROUGH, value = "false"))
```

**Alternative: use `Set<>` instead of `List<>` for the association** — Hibernate de-duplicates automatically (but you lose ordering guarantees).

**Best practice for two collections:** fetch them in two separate queries — `JOIN FETCH` the first, then `@BatchSize` or a second query for the second. Hibernate's first-level cache means parent entities are not re-fetched.

---

## `@OneToMany(fetch = EAGER)` is always wrong

Setting `fetch = FetchType.EAGER` directly on a `@OneToMany` (or `@ManyToMany`) association is an anti-pattern with two distinct problems:

1. **Always loads, even when not needed.** Every parent query fetches the entire child collection — no call site can opt out. A `findById` to check order status loads all items too.
2. **Cartesian product with multiple eager collections.** Two EAGER `@OneToMany` collections cause Hibernate to join both in a single query. An order with 10 items and 5 payments returns 50 rows.

The JPA spec warns that EAGER on `@OneToMany` is a hint, not a guarantee — providers may use multiple queries anyway, so you get the always-loads cost without even the "one query" benefit.

**Rule:** `@OneToMany` and `@ManyToMany` must always be `LAZY`. Fetch eagerly per-query with `JOIN FETCH` or `@EntityGraph` when the caller actually needs the data.

---

## When EAGER is worse

`@ManyToOne(fetch = EAGER)` seems cheap — just one related entity. But consider:

```java
@Entity
public class Order {
    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;
}

@Entity
public class OrderItem {
    @ManyToOne(fetch = FetchType.EAGER)  // EAGER
    private Product product;
}
```

Loading 100 orders with `JOIN FETCH o.items`: Hibernate loads all items, and each item's EAGER `product` is also fetched — either as additional joins (bloating the result set) or per-item queries. EAGER on `@ManyToOne` inside a `@OneToMany` is a hidden N+1.

Rule of thumb: make all associations `LAZY` and fetch what you need per use case. EAGER removes control from the caller.

---

## Gotchas / Trick questions

1. **"If everyone recommends LAZY, won't N+1 always happen?"** No — LAZY is the *safe default*, EAGER is the *dangerous default*. A login API on a User with orders/reviews/addresses/wishlist/payments only needs name + email — LAZY means nothing extra loads. N+1 happens only when a developer loads a list and then accesses a lazy association per element without planning the fetch — the bug is ignoring the access pattern, not LAZY itself. Senior approach: default LAZY, explicit fetch (`JOIN FETCH` / `@EntityGraph` / `@BatchSize`) per use case.
2. **"Entities loading each other in a loop (1 → 2 → 1 → 2 …) — is that N+1?"** No — different problem. A bidirectional mapping (`User → orders`, `Order → user`) serialized naively traverses User → Orders → User → Orders … until `StackOverflowError`. That's the **circular reference problem** (infinite object graph traversal), typically hit during JSON serialization — fixed with `@JsonIgnore` / `@JsonManagedReference`+`@JsonBackReference` or DTOs, not with fetch strategies.
3. **"Does the second-level cache fix N+1?"** Not directly — it reduces repeated DB hits for the same entities across sessions, but the first cold pass still fires N+1 queries. Use batch/fetch strategies for N+1; the cache is a complementary optimization.

## Quick recall

**Q. What is the N+1 problem?**
A. Loading N root entities then accessing a lazy association on each triggers N additional queries — 1 for the list, N for the associations.

**Q. `JOIN FETCH` vs `@EntityGraph` — which to prefer?**
A. They are equivalent under the hood. `@EntityGraph` is cleaner for repository methods and avoids duplicating JPQL; `JOIN FETCH` gives finer control for complex queries.

**Q. Why does `JOIN FETCH` on two `@OneToMany` collections cause problems?**
A. It creates a Cartesian product in the result set — rows multiply across both collections. Fix with separate queries or `DISTINCT` + `PASS_DISTINCT_THROUGH = false`.

**Q. What does `@BatchSize(size=50)` do?**
A. When lazy associations are accessed, Hibernate loads them in batches using SQL `IN` clauses — reduces N+1 from N+1 queries to ceil(N/50)+1 queries without changing JPQL.

**Q. When does `LazyInitializationException` occur, and what are the fixes?**
A. When a lazy association is accessed after the Hibernate session is closed (outside `@Transactional`). Fix by keeping access inside the transaction, using a DTO projection, or explicitly fetching in the query. Avoid Open Session in View — it masks the problem while leaking DB connections into the presentation layer.

**Q. Why is `@ManyToOne(fetch = EAGER)` dangerous inside a `@OneToMany` list?**
A. Every parent entity load fetches all children (items), and each child's EAGER `@ManyToOne` is also fetched — producing a hidden N+1 or a massive Cartesian product join.

**Q. What is the best default fetch strategy?**
A. Make all associations `LAZY` and use `JOIN FETCH` / `@EntityGraph` explicitly per query. EAGER bakes in a fetch strategy that may be wrong for most callers.
