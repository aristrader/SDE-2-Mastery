---
order: 40
---

# Lazy vs Eager Loading & N+1

Lazy/eager loading controls when associations are fetched from the database. For interviews, focus on defaults, `LazyInitializationException`, N+1, and the main fixes.

---

## FetchType defaults

| Association | Default |
|---|---|
| `@OneToMany` | `LAZY` |
| `@ManyToMany` | `LAZY` |
| `@ManyToOne` | `EAGER` |
| `@OneToOne` | `EAGER` |

Practical rule:

> Prefer `LAZY` for associations and fetch what the use case needs explicitly.

`EAGER` sounds convenient, but it bakes one fetch plan into every query. That often becomes expensive later.

---

## Lazy loading

With lazy loading, Hibernate does not load the association immediately.

```java
Order order = orderRepository.findById(id).orElseThrow();
// order loaded

List<OrderItem> items = order.getItems();
// collection may still be a lazy proxy

items.size();
// SQL runs here if the transaction/session is still open
```

If the transaction/session is already closed, accessing the lazy association can throw `LazyInitializationException`.

---

## LazyInitializationException

Common bad flow:

```java
public Order getOrder(Long id) {
    return orderRepository.findById(id).orElseThrow();
}

// later in controller/JSON serialization
order.getItems().size(); // LazyInitializationException
```

The service returned an entity, the transaction ended, and the controller/serializer touched lazy data.

Better:

```java
@Transactional(readOnly = true)
public OrderDetails getOrder(Long id) {
    Order order = orderRepository.findWithItems(id).orElseThrow();
    return OrderDetails.from(order);
}
```

Load what you need inside the service transaction and return a DTO.

---

## N+1 problem

N+1 means:

1. one query loads N parent rows
2. one extra query runs for each parent's lazy association

```java
List<Order> orders = orderRepository.findAll(); // 1 query

for (Order order : orders) {
    order.getItems().size(); // N more queries
}
```

For 100 orders, this can become 101 queries.

The code is correct functionally, but slow.

---

## Fix 1: JOIN FETCH

Use `JOIN FETCH` when this query always needs the association.

```java
@Query("""
    select o
    from Order o
    join fetch o.items
    where o.id = :id
    """)
Optional<Order> findWithItems(Long id);
```

Good for clear, query-specific fetch needs.

Avoid fetch-joining multiple collections in one query unless you know the row multiplication impact.

---

## Fix 2: @EntityGraph

`@EntityGraph` fetches associations without writing JPQL.

```java
@EntityGraph(attributePaths = "items")
Optional<Order> findById(Long id);
```

Useful when the derived query is fine but you want to fetch one or two associations.

---

## Fix 3: DTO projection

For read-only API responses, DTO projections are often the cleanest fix.

```java
public record OrderSummary(Long id, String status, String customerName) {
}

@Query("""
    select new com.example.OrderSummary(o.id, o.status, c.name)
    from Order o
    join o.customer c
    where o.status = :status
    """)
List<OrderSummary> findOrderSummaries(OrderStatus status);
```

Benefits:

- no lazy proxies in the response
- no accidental entity serialization
- fetches only needed columns

---

## Why EAGER is risky

```java
@OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
private List<OrderItem> items;
```

Problem:

- every order query loads items, even when not needed
- multiple eager collections can multiply rows badly
- caller cannot opt out

Safer approach:

```java
@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
private List<OrderItem> items;
```

Then fetch explicitly with `JOIN FETCH`, `@EntityGraph`, or DTO projection when needed.

---

## Open Session in View

Spring Boot may keep the Hibernate session open during the web request with Open Session in View.

That can hide `LazyInitializationException`, but it often causes database queries from controllers or JSON serialization.

Avoid relying on Open Session in View. Fetch data in the service/repository layer and return DTOs.

---

## N+1 vs circular JSON

These are different problems.

N+1:

- too many database queries
- fixed with fetch joins, entity graphs, batch fetching, or projections

Circular JSON:

- `User -> orders -> user -> orders...`
- can cause infinite serialization
- fixed with DTOs or Jackson annotations like `@JsonIgnore`

DTOs often solve both for REST APIs.

---

## Quick recall

**Q. What is N+1?**  
A. One query loads N parents, then N extra queries load associations.

**Q. What causes `LazyInitializationException`?**  
A. Accessing a lazy association after the transaction/session is closed.

**Q. Main fixes for N+1?**  
A. `JOIN FETCH`, `@EntityGraph`, or DTO projections.

**Q. Why avoid `EAGER` by default?**  
A. It always loads the association and removes control from the query/use case.

**Q. Best REST API approach?**  
A. Load required data in the service/repository layer and return DTOs.

**Q. N+1 vs circular JSON?**  
A. N+1 is a database query problem. Circular JSON is serialization recursion.
