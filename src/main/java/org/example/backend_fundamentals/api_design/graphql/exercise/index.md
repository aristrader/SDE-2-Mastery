---
order: 10
search: false
---

# Exercises: GraphQL Backend Engineering

Practical interview drills focusing on GraphQL schema design, nested resolver execution mechanics, DataLoader batching, and query complexity defense.

---

## Exercise: graphql-schema-resolver-boundaries - Schema Design & Field Resolvers

### Context
You are building an API gateway that aggregates data from an Account Service and an Order Service. Clients request user profiles alongside their recent orders and payment statuses.

### Constraints
- The schema must distinguish between root query resolvers and nested type field resolvers.
- Internal database models cannot be exposed directly; field mapping and field-level authorization must be handled cleanly.
- Nullability must be intentionally designed: a failure in fetching optional order history must not fail the entire user profile query.

### Deliverables
1. **Schema Definition (SDL)**: Define `Query`, `User`, and `Order` types with appropriate non-null (`!`) constraints for essential identifiers while keeping failure-prone sub-graphs nullable.
2. **Resolver Boundary Plan**: Specify which fields are resolved by the parent query resolver versus child field resolvers (`User.orders`).
3. **Partial Failure Contract**: Document how the GraphQL response structure formats data and errors when `User.orders` fails downstream while `User.profile` succeeds.

---

## Exercise: graphql-dataloader-n1 - Request-Scoped DataLoader & Batch Resolution

### Context
A query fetches a list of users and their respective organization details:
```graphql
query GetUsers {
  users(limit: 50) {
    id
    name
    organization {
      id
      name
    }
  }
}
```
A naive nested resolver for `User.organization` triggers 50 separate downstream queries ($1 + N$ queries).

### Constraints
- Batch all organization lookups into a single multi-key downstream call (`findByIds(List<ID> ids)`).
- Ensure the DataLoader instance is strictly request-scoped to prevent cross-request cache pollution and authorization leaks.
- DataLoader batch function must preserve 1:1 positional indexing and list length between requested keys and returned entities, handling missing records gracefully.

### Deliverables
1. **Execution Timeline**: Trace how the execution engine traverses the AST, defers field resolution, and triggers the batch dispatch function.
2. **DataLoader Configuration**: Define the batch loader contract, detailing key collation, asynchronous promise/completable future resolution, and mapping strategy.
3. **Request Lifecycle Isolation**: Explain how the DataLoader registry is instantiated per HTTP request context and cleaned up after execution.

---

## Exercise: graphql-guardrails-depth-complexity - Query Depth & Complexity Guardrails

### Context
Because GraphQL allows clients to request arbitrary nested graphs, attackers or misconfigured clients can submit cyclic queries (e.g., `author -> books -> author -> books...`) or wide pagination combinations that trigger server CPU spikes and database denial-of-service (DoS).

### Constraints
- Rejection must occur during the static validation phase before any resolver or database query is executed.
- Enforce both a maximum query depth limit and a calculated query complexity score based on AST analysis.
- Cost calculation for list fields must account for slicing arguments (e.g., `first: N` multipliers).
- Production mobile and web clients should utilize persisted queries (query whitelisting).

### Deliverables
1. **Static Validation Rules**: Define a depth calculation algorithm and a field-cost formula (e.g., scalar cost = 1, relation cost = 5, paginated list = $N \times \text{item cost}$).
2. **Guardrail Policy**: Set concrete rejection thresholds (e.g., max depth = 6, max complexity = 200) and specify the GraphQL error response returned upon violation.
3. **Persisted Query Workflow**: Outline the hash-based persisted query lifecycle between build pipeline, edge cache, and backend validation layer.
