---
order: 20
search: false
---

# Solutions: GraphQL Backend Engineering

Complete reference solutions for schema-resolver boundary modeling, request-scoped DataLoader batching, and query complexity defense guardrails.

---

## Solution: graphql-schema-resolver-boundaries - Schema Design & Field Resolvers

### 1. Schema Definition (SDL)
```graphql
type Query {
  user(id: ID!): User
}

type User {
  id: ID!
  name: String!
  email: String!
  orders: [Order!] # Nullable field wrapper allows partial failure; items are non-null
}

type Order {
  id: ID!
  totalCents: Int!
  status: OrderStatus!
  createdAt: String!
}

enum OrderStatus {
  PENDING
  PAID
  CANCELLED
}
```

### 2. Resolver Boundary Architecture
- **Root Query Resolver (`Query.user`)**:
  - Handles the entrypoint by extracting the target user ID and authenticating the request context.
  - Calls Account Service / User Repository to fetch core attributes (`id`, `name`, `email`).
  - Returns a partial DTO/entity containing user fields without eagerly joining order data.
- **Child Field Resolver (`User.orders`)**:
  - Executes lazily only if the incoming selection set explicitly requests `orders`.
  - Receives the parent `User` object as the resolver source/parent argument.
  - Enforces field-level authorization (e.g., verifying the caller matches `user.id` or has admin scopes).
  - Calls Order Service using `user.id`.
- **Field Execution Boundary**:
  - Prevents overfetching when clients query only `user { id name }`.
  - Isolates downstream domain service dependencies to their respective field resolvers.

### 3. Partial Failure Contract & Nullability Bubbling
When `User.orders` encounters a downstream service timeout or failure while `Query.user` succeeds:
- **Nullability Isolation**: Because `User.orders` is defined as nullable (`[Order!]` instead of `[Order!]!`), the GraphQL execution engine sets `orders: null` instead of bubbling the null up to invalidate the entire `User` or `data` payload.
- **Error Response Structure**:
```json
{
  "data": {
    "user": {
      "id": "usr_101",
      "name": "Alex Mercer",
      "email": "alex@example.com",
      "orders": null
    }
  },
  "errors": [
    {
      "message": "Downstream order service unavailable",
      "locations": [{ "line": 5, "column": 5 }],
      "path": ["user", "orders"],
      "extensions": {
        "code": "DOWNSTREAM_SERVICE_ERROR",
        "service": "order-service",
        "retryable": true
      }
    }
  ]
}
```

---

## Solution: graphql-dataloader-n1 - Request-Scoped DataLoader & Batch Resolution

### 1. AST Traversal & Dispatch Timeline
1. **Level-by-Level Execution**:
   - The execution engine resolves root field `users(limit: 50)`, returning a list of 50 `User` objects.
2. **Deferred Promise Registration**:
   - As the engine processes `User.organization` for each user, the field resolver does not immediately query the database.
   - Instead, it invokes `orgDataLoader.load(user.orgId)`, returning a deferred `CompletableFuture` / `Promise` and queueing `user.orgId` into the DataLoader's internal batch key set.
3. **Dispatch Phase**:
   - Once all field resolvers at the current AST breadth have queued their keys, the execution engine triggers DataLoader dispatch.
   - Keys are deduplicated (e.g., 50 users mapped to 5 distinct organization IDs).
   - The single batch function `findByIds(List<ID> uniqueIds)` executes one downstream multi-get query.
4. **Promise Resolution & Caching**:
   - The batch result resolves the 50 individual promises, continuing child field execution across the AST.

### 2. DataLoader Configuration & Positional Index Contract
DataLoader mandates a strict 1:1 invariant between input keys and returned results:
- **Batch Function Contract**:
  - Input: `List<K>` containing $M$ keys.
  - Output: `CompletableFuture<List<V>>` containing exactly $M$ elements in the exact order of input keys.
  - If a key does not exist in the database, the batch function must insert `null` (or a specific `Exception`) at that key's corresponding index.
- **Java / Backend Implementation Pattern**:
```java
// Batch loader function implementation
BatchLoader<String, Organization> orgBatchLoader = keys -> CompletableFuture.supplyAsync(() -> {
    // 1. Fetch records matching requested keys in a single SQL / RPC call
    Map<String, Organization> orgMap = orgService.getOrganizationsByIds(keys)
        .stream()
        .collect(Collectors.toMap(Organization::getId, Function.identity()));

    // 2. Preserve exact input ordering and list length, returning null for missing entities
    return keys.stream()
        .map(orgMap::get)
        .collect(Collectors.toList());
});
```
- **Error Propagation**: If an individual entity lookup fails, individual promise rejection or null assignment isolates the failure to that single field without failing the batch for all other keys.

### 3. Request Lifecycle Isolation
- **Request-Scoped Instantiation**:
  - A new `DataLoaderRegistry` instance is created inside the HTTP interceptor / context factory for every incoming HTTP request.
  - The registry is attached to GraphQL execution input context (`ExecutionInput.dataLoaderRegistry(registry)`).
- **Security & Authorization Isolation**:
  - DataLoader memoization cache lives strictly within the lifecycle of a single request.
  - Prevents cross-request cache leaks where User B could read User A's cached entity without authorization checks.
- **Cleanup**:
  - The request registry and cached futures are garbage-collected when the HTTP response completes.

---

## Solution: graphql-guardrails-depth-complexity - Query Depth & Complexity Guardrails

### 1. Static AST Validation Rules
Validation runs during document analysis before field execution:
- **Maximum Query Depth (AST Depth Tree)**:
  - Calculated by traversing the operation selection set recursively.
  - Scalars at depth $k$ evaluate to depth $k$; nested object types increment depth counter $k + 1$.
  - Fragments and inline spreads are flattened into their target types.
- **Query Complexity Score Algorithm**:
  - Assign baseline weights: Scalar = `1`, Object / Relation = `5`, Mutation root = `10`.
  - Multiplier for Paginated Lists: $\text{Field Cost} = \text{Base Cost} + (\text{limit Argument} \times \text{Child Selection Cost})$.
  - Formula:
    $$\text{Total Cost} = \sum_{\text{fields}} \left( \text{Weight}(f) + \text{Multiplier}(f) \times \sum \text{ChildCosts} \right)$$

### 2. Guardrail Policy & Error Rejection
- **Policy Thresholds**:
  - `Max Query Depth`: `6` levels.
  - `Max Query Complexity`: `200` points.
- **Static Rejection Behavior**:
  - If calculated depth > 6 or complexity > 200, execution halts immediately during validation.
  - No database queries, network calls, or resolvers are invoked.
- **Standard Rejection Response (HTTP 200 or HTTP 400 with GraphQL error)**:
```json
{
  "errors": [
    {
      "message": "Query complexity limit exceeded: calculated cost 340 exceeds maximum threshold 200",
      "extensions": {
        "code": "QUERY_COMPLEXITY_EXCEEDED",
        "calculatedComplexity": 340,
        "maxComplexity": 200
      }
    }
  ]
}
```

### 3. Persisted Query (APQ & Whitelisting) Lifecycle
1. **Build Phase (Strict Whitelisting / Production Manifest)**:
   - Client build pipeline extracts all GraphQL operations from frontend codebase.
   - Operations are normalized and hashed using SHA-256.
   - Hash manifest mapping `{ "<sha256-hash>": "<normalized-query-string>" }` is deployed to backend/CDN storage.
2. **Runtime Execution (Automatic Persisted Queries - APQ)**:
   - Client sends hash only: `GET /graphql?extensions={"persistedQuery":{"version":1,"sha256Hash":"a1b2c3..."}}`.
   - **Edge / Gateway Cache Hit**: CDN / API Gateway retrieves query string by hash, verifies cache, and serves or proxies request.
   - **Cache Miss**: Client receives `PersistedQueryNotFound` error and retries with both query body and hash (`POST /graphql`). Backend validates hash matches query body and stores mapping in Redis / in-memory cache.
3. **Security Invariant**:
   - In locked-down production environments, arbitrary raw query execution can be disabled entirely; only known hashes in the manifest are permitted.

---

## Quick recall

1. **Why does GraphQL field resolution naturally produce $1 + N$ queries?**
   Each child resolver executes independently per parent list item without awareness of sibling field resolutions unless batched by a mechanism like DataLoader.
2. **Why must a DataLoader batch loader return an array of equal length and order?**
   DataLoader matches resolved values back to input keys by index; mismatching order or length causes silent data corruption across callers.
3. **Why must DataLoader instances be request-scoped?**
   To enforce per-request authorization boundaries and avoid stale/cross-tenant cached data leaks across concurrent user requests.
4. **When should query depth/complexity checks execute?**
   During the AST document validation phase prior to execution, preventing resource exhaustion and database load on malicious queries.
