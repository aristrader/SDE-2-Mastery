---
order: 40
---

# API Technologies — REST vs GraphQL vs gRPC Comparison

## REST vs GraphQL vs gRPC (Interview Version)

When asked to compare API technologies in an SDE2/System Design interview, you are expected to contrast them across coupling, performance, and best use-cases. 

### Architecture Characteristics

| Type | Coupling | Chattiness | Performance | Complexity | Caching | Codegen | Discoverability | Versioning |
|---|---|---|---|---|---|---|---|---|
| **REST** | Low | High | Good | Medium | Great | Bad | Good | Easy |
| **GraphQL** | Medium | Low | Good | High | Custom | Good | Good | Custom |
| **gRPC** | High | Medium | Great | Low | Custom | Great | Bad | Hard |

### Practical Trade-offs

| Feature | REST | GraphQL | gRPC |
|---|---|---|---|
| **Learning Curve** | Easy | Medium | Medium |
| **Human Readable** | Yes | Yes | No |
| **Browser Support** | Excellent | Excellent | Limited |
| **Performance** | Good | Good | Excellent |
| **Over-fetching** | Yes | No | No |
| **Streaming** | No | Limited | Excellent |
| **Public APIs** | Excellent | Good | Poor |
| **Internal Microservices** | Good | Rare | Excellent |

## When to choose which?

Interviewers expect you to know *why* you pick a specific technology.

### When would you choose REST?
- **Public APIs**: It has the broadest ecosystem support and is universally understood by external partners.
- **Simplicity**: Best when the domain naturally maps to resources (CRUD operations).
- **Caching**: Easy to cache at the HTTP layer (CDN, reverse proxies) because of standard HTTP GET semantics.

### When would you choose GraphQL?
- **Mobile clients**: Bandwidth and battery optimization by fetching exactly what's needed in one request.
- **Complex frontend requirements**: When the UI aggregates data across multiple disparate domain entities.
- **Avoid over-fetching/under-fetching**: Eliminates the "multiple network calls" penalty of REST for nested relationships.

### When would you choose gRPC?
- **Service-to-service communication**: High throughput and low latency for internal microservices.
- **Streaming requirements**: Built-in support for continuous streams (e.g., chat apps, IoT telemetry).
- **Strong contracts**: Code generation across polyglot environments with strict `.proto` definitions.

## Common Real-World Architecture

A standard hybrid approach used in many large-scale systems:

```text
       Mobile / Web Client
               │
               ▼
          API Gateway
               │
   (REST or GraphQL over HTTP/1.1)
               │
    ┌──────────┴──────────┐
    ▼                     ▼
 UserSvc               OrderSvc
    │                     │
    └─────── gRPC ────────┘
```

**Why this works:**
- **External clients** use REST/GraphQL for ease of integration, browser compatibility, and caching.
- **Internal microservices** use gRPC (over HTTP/2 with Protobuf) for high-speed, binary, low-latency communication.

## gRPC Deep Dive: Why it's fast & Streaming capabilities

### Why gRPC is Fast
1. **Binary Protocol**: Protobuf creates significantly smaller payloads than JSON.
2. **HTTP/2**: Supports multiplexing (multiple requests over a single TCP connection), header compression, and streaming.
3. **Generated Code**: Deserialization is natively compiled; no reflection-heavy JSON parsing is required.

### Streaming Types (A huge advantage over REST)
REST is strictly Request → Response → Done. gRPC supports continuous streams:

1. **Unary**: 1 Request → 1 Response (Most common).
2. **Server Streaming**: 1 Request → Many Responses (e.g., getting real-time stock updates).
3. **Client Streaming**: Many Requests → 1 Response (e.g., uploading large file chunks).
4. **Bidirectional Streaming**: Many Requests ↔ Many Responses (e.g., WhatsApp chat, multiplayer gaming).

## Protocol Mechanics & Deep Dive Trade-offs

### 1. REST Method Semantics & HTTP-Native Affordances

REST leverages HTTP's built-in protocol guarantees (RFC 9110):

- **Safe vs Idempotent Methods**:
  - **Safe (Read-Only)**: `GET`, `HEAD`, `OPTIONS`, `TRACE`. Calling them must not alter server state.
  - **Idempotent (Repeatable without side-effect accumulation)**: `GET`, `HEAD`, `PUT`, `DELETE`, `OPTIONS`, `TRACE`. Making $N > 1$ identical requests leaves the server in the identical state as making 1 request.
  - **Non-Idempotent**: `POST` (submitting payloads to append new resources) and `PATCH` (partial updates, unless combined with conditional precondition headers like `If-Match`).
- **HTTP Status Codes as First-Class Signals**:
  - Standard status ranges (`2xx` Success, `3xx` Redirection, `4xx` Client Error, `5xx` Server Error) allow intermediate proxies, API gateways, and monitoring infrastructure to understand request outcomes without inspecting payload bodies.
- **HTTP-Native Caching & URI Identification**:
  - Distinct URI paths represent unique addressable resources.
  - Native headers (`Cache-Control: max-age=...`, `ETag` / `If-None-Match`, `Last-Modified` / `If-Modified-Since`) allow browser caches, CDNs, and forward/reverse proxies to cache responses at the network edge with standardized invalidation.

### 2. GraphQL Execution Dynamics: Resolver N+1, Depth Limits & Caching

GraphQL replaces fixed server endpoints with an expressive, client-driven query graph (GraphQL Spec):

- **Resolver Execution & the N+1 Problem**:
  - Each field in a GraphQL query executes an independent resolver function.
  - In nested queries (e.g., fetching 50 `posts` and each post's `author`), a naive server runs 1 query for posts and 50 separate queries for authors ($1 + N$ queries).
  - **Mitigation (DataLoader Pattern)**: Defers individual database fetches within a single tick of the event loop, batching them into a single query (`WHERE id IN (...)`) and caching results per-request.
- **Query Depth & Complexity Limits (Security & DoS Prevention)**:
  - Clients can craft arbitrarily deep or cyclic queries (e.g., `user { friends { friends { friends ... } } }`), exhausting CPU, memory, and database connection pools.
  - **Mitigations (OWASP GraphQL Cheat Sheet)**:
    - **Max Query Depth**: Reject queries that exceed a defined nesting depth threshold.
    - **Query Cost Analysis**: Assign complexity weights to fields and reject queries exceeding a max total budget.
    - **Persisted Queries**: Store approved query hashes on the server; clients send only query IDs in production.
- **The Caching Trade-off**:
  - Because GraphQL typically routes all operations via `POST /graphql` to a single URI, intermediate HTTP proxies and CDNs cannot key caches on URLs.
  - Caching shifts to client-side normalized caches (e.g., Apollo Client InMemoryCache) or persisted queries converted to cacheable HTTP `GET` requests.

### 3. gRPC Remote-Call Boundary & HTTP/2 Load Distribution

gRPC is designed for high-efficiency distributed communication (grpc.io):

- **The Remote-Call Boundary (Never Treat RPC as Local)**:
  - RPC presents remote method invocations with the syntax of local function calls, but crossing process/network boundaries introduces latency, packet drops, serialization overhead, network partitions, and partial failures (Fallacies of Distributed Computing).
- **Resilience Controls (Deadlines, Cancellation & Partial Failures)**:
  - **Deadlines / Timeouts**: Propagated across the call graph via context (`grpc-timeout` header). Each downstream handler must observe the deadline and stop or cancel its own work promptly; propagation is not a guarantee that arbitrary work halts instantly.
  - **Context Cancellation**: Client-initiated cancellation is signalled down the dependency chain; handlers and downstream clients must cooperate by observing the signal and aborting cancellable in-flight work.
  - **Partial Failures & Rich Errors**: Decouples wire transmission from application errors using `google.rpc.Status` (transporting machine-readable error details rather than generic HTTP status codes) alongside exponential backoff with jitter for retries.
- **HTTP/2 Long-Lived Connection Load-Balancing Nuance**:
  - HTTP/2 multiplexes hundreds of concurrent RPC streams across a single persistent TCP connection.
  - **The L4 Load Balancing Trap**: Traditional Layer-4 (TCP) load balancers route the single TCP handshake to one backend pod. All subsequent multiplexed RPCs on that connection flood that single instance, creating severe hot-spotting.
  - **Mitigation**:
    - **L7 Load Balancing**: Application-layer proxies (e.g., Envoy, Traefik) terminate HTTP/2 connections and distribute individual RPC streams across backends.
    - **Client-Side Load Balancing**: The gRPC client uses name resolvers (e.g., Kubernetes headless service, DNS) and channel load-balancing policies (e.g., round-robin) to open connections to multiple backend instances directly.

### 4. Schema Evolution & Compatibility Comparison

| Protocol | Compatibility Model | Evolution Strategy | Breaking Change Management |
|---|---|---|---|
| **REST (JSON)** | Structural JSON contracts | Additive optional fields are backward compatible | Versioning via URL path (`/v1/users`), query parameter (`?v=1`), or custom header (`Accept: application/vnd.company.v1+json`). |
| **GraphQL** | Strongly typed schema | Continuous additive evolution; clients select only needed fields | Mark fields with `@deprecated(reason: "...")`. Schema evolves without versioned endpoints; obsolete fields are retired after usage drops to zero. |
| **gRPC (Protobuf)** | Compact binary wire format keyed by field tag numbers | Backward & forward compatible as long as field numbers and wire types are preserved | Old field names can be updated if tag numbers match; removed fields must be declared `reserved` to prevent reuse of their tags/names. |

## Quick recall

**Q. Why do most public APIs still use REST?**
A. Broadest ecosystem support, easiest to integrate for external clients, natively supported by all browsers, and excellent HTTP-level caching.

**Q. What problem does GraphQL solve over REST?**
A. It eliminates over-fetching (getting unnecessary fields) and under-fetching (requiring multiple round trips to get related data).

**Q. Why is gRPC faster than REST?**
A. Uses compact binary payloads (Protobuf) instead of text (JSON), runs on HTTP/2 (multiplexing, header compression), and uses generated code without reflection.

**Q. What are the 4 streaming types in gRPC?**
A. Unary, Server streaming, Client streaming, and Bidirectional streaming.

**Q. Describe a common hybrid API architecture.**
A. REST or GraphQL exposed via API Gateway to external clients, while internal microservices communicate with each other using gRPC.

**Q. What is the difference between safe and idempotent HTTP methods in REST?**
A. Safe methods (`GET`, `HEAD`, `OPTIONS`) are read-only and never mutate server state. Idempotent methods (`GET`, `PUT`, `DELETE`) can be executed repeatedly with the identical net effect on server state as a single invocation.

**Q. How do you resolve the N+1 problem and mitigate DoS queries in GraphQL?**
A. Use the DataLoader pattern (batching and memoization) to collapse $N$ nested queries into a single batched database fetch. Protect against DoS using max query depth limits, static query complexity/cost budgets, and persisted queries.

**Q. Why does HTTP/2 multiplexing cause hot-spotting behind traditional L4 load balancers in gRPC?**
A. L4 load balancers distribute TCP connections, not individual requests. Because HTTP/2 multiplexes all RPCs over a single long-lived TCP connection, all traffic flows to the single backend pod that accepted the connection. Resolving this requires L7 proxy balancing or gRPC client-side load balancing.

**Q. How does Protobuf ensure backward and forward compatibility during schema changes?**
A. Protobuf serializes data using numeric field tags rather than field names. New optional fields can be introduced without breaking old clients, and retired tags/fields are marked `reserved` to prevent accidental tag collisions.

## References & Authoritative Sources

- **RFC 9110**: *HTTP Semantics* (IETF Standard for HTTP methods, status codes, safety, and idempotency).
- **GraphQL Specification**: *GraphQL Working Group* (Execution model, resolver semantics, schema directives, and validation).
- **grpc.io Documentation & Concepts**: *The Linux Foundation / gRPC Authors* (HTTP/2 transport, deadlines, cancellation, status model, and client/L7 load balancing).
- **MDN Web Docs**: *HTTP Methods, Caching Headers, and Conditional Requests*.
- **OWASP GraphQL Cheat Sheet**: *OWASP Foundation* (Query depth limiting, complexity cost calculation, and persisted query defenses).
- **System Design Primer**: *Dong Nguyen & Donne Martin* (API paradigm trade-offs, network caching, and distributed communication models).
