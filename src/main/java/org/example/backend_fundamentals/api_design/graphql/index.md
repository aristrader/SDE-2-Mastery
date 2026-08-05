---
order: 10
---

# Graph Databases & GraphQL — Related in Name Only

**The misconception first:** GraphQL and graph databases are NOT related. GraphQL is an **API technology**; graph databases are a **database category**. The shared word "graph" is a coincidence of naming.

## Graph databases

### Fraud detection — the canonical use case

Fraud is often about **relationships**, not individual records. Each account looks fine in isolation; the pattern only appears when you connect them:

```text
Alice   → Device D1
Bob     → Device D1     ← shared device: suspicious
Charlie → Device D1
```

**Circular money movement:**

```text
A → B → C → D → A
```

Graph databases can detect cycles like this efficiently.

**Fraud use cases:** shared devices, shared addresses, money-laundering chains, circular transactions, networks of related accounts.

**Interview answer:** graph databases are useful in fraud detection because fraud often involves hidden relationships among accounts, devices, IPs, customers, and transactions — graph traversal makes these patterns easy to discover.

## GraphQL

### What it is

A query language for APIs that lets clients request **exactly the data they need**.

REST — multiple endpoints:

```text
GET /users/123
GET /users/123/orders
```

GraphQL — single request:

```graphql
{
  user(id: 123) {
    name
    orders {
      amount
    }
  }
}
```

### How the backend works (vs REST routing)

In REST, the URL routes to a controller. GraphQL instead exposes **one endpoint** — `POST /graphql` — and routes by query shape:

```text
REST:     URL  → Controller
GraphQL:  Query → Schema → Resolvers
```

**Schema** declares what's queryable:

```graphql
type Query {
  user(id: ID!): User
}
```

**Resolver** is the function that fetches it:

```javascript
user: (_, args) => {
  return userRepository.findById(args.id);
}
```

A frontend query for `user(id:123){ name }` invokes the `user` resolver.

### Aggregating multiple services

A GraphQL server can call the User Service, Order Service, and Payment Service, combine the results, and return one response — it's an aggregation layer.

### GraphQL and N+1

Classic problem. For:

```graphql
{
  users {
    name
    orders { amount }
  }
}
```

a naive implementation does *get users, then for each user get orders* — N+1 again. **Common solution: DataLoader**, which batches the per-user requests into one `WHERE user_id IN (...)` query. (General N+1 mechanics: `spring/jpa_lazy_eager/LazyEagerN1.md`.)

### The mental model

The GraphQL server is simply a backend component. Instead of writing many REST endpoints (`GET /user`, `GET /orders`), you write resolvers. GraphQL parses the request, figures out which fields were requested, invokes the appropriate resolvers, fetches the data, and returns exactly the requested structure.

## Gotchas / Trick questions

1. **"GraphQL must use a graph database."** No — GraphQL is an API query language; it can sit on top of SQL, NoSQL, other services, anything. Graph databases (Neo4j, Neptune) are a storage category.
2. **"GraphQL eliminates the N+1 problem."** The opposite — naive resolvers *reintroduce* it (one query per nested field per parent). DataLoader-style batching is the standard fix.
3. **"How does GraphQL route without URLs?"** Single `POST /graphql` endpoint; the query body is parsed against the schema and dispatched to resolvers — query shape replaces URL routing.

## Quick recall

**Q. GraphQL vs graph database?**
A. GraphQL is an API query language; a graph database is a database category. Unrelated despite the name.

**Q. Why are graph databases good for fraud detection?**
A. Fraud lives in hidden relationships (shared devices/addresses, circular transfers) — graph traversal finds these patterns efficiently.

**Q. How does a GraphQL backend know what to execute?**
A. One `POST /graphql` endpoint; the query is validated against the schema and each requested field is resolved by its resolver function.

**Q. How does N+1 appear in GraphQL, and what's the fix?**
A. Naive nested resolvers fire one query per parent entity; DataLoader batches them into `WHERE id IN (...)` queries.

**Q. What can a GraphQL server sit in front of?**
A. Anything — databases, REST services, multiple microservices — aggregating results into one response shaped exactly like the query.


