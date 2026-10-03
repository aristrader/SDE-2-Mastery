---
order: 10
search: false
---

# API Design Exercises

Practice core API design concepts: REST resource conventions, status codes, idempotency semantics, stateful session scaling vs stateless JWT trade-offs, and API paradigm selection.

---

## Exercise: rest-resource-status-idempotency - REST Contract & Semantics Design

### Context
Design an order and payment API contract for an e-commerce platform transitioning to a standard RESTful interface.

### Requirements & Constraints
1. **Endpoint Modeling**: Define endpoints for the following operations without using action verbs in URI paths:
   - List customer orders (with optional status filtering and pagination).
   - Retrieve a specific order by ID.
   - Create a new draft order.
   - Apply a partial update to the order delivery address without modifying other fields.
   - Replace the entire customer delivery profile record for the order.
   - Process payment checkout for an order.
   - Cancel an unpaid order.
2. **HTTP Semantics**: For each endpoint, determine:
   - The correct HTTP method (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
   - Whether the method is **safe** (read-only, no server-side state mutation).
   - Whether the method is **idempotent** (multiple identical requests yield the same server state).
   - Primary success status code (`200 OK`, `201 Created`, `204 No Content`).
   - Relevant client/server error status codes (`400`, `401`, `403`, `404`, `409`, `422`, `429`, `500`).
3. **Idempotency Strategy**: Define the contract and header mechanism used to prevent duplicate charges when a client retries a payment POST request after a network timeout.

### Expected Deliverable
- An endpoint contract table containing: `Operation`, `HTTP Verb`, `URI Path`, `Safe (Yes/No)`, `Idempotent (Yes/No)`, `Success Status Code`, and `Key Error Status Codes`.
- A concise specification (3–5 bullet points) explaining how the server validates and handles client-supplied idempotency keys on payment submission retries.

---

## Exercise: session-vs-jwt-scaling - Stateful Session Scaling vs JWT Trade-Offs

### Context
A monolith running on a single server maintains user login state via in-memory sessions (`JSESSIONID -> UserSession`). The engineering team is scaling the backend horizontally to 10 instances behind a round-robin Layer-7 load balancer.

### Requirements & Constraints
Analyze three architectural strategies to support horizontal authentication:
1. **Sticky Sessions (Session Affinity)** on the load balancer.
2. **Distributed Shared Session Store** (e.g., centralized Redis cluster).
3. **Stateless JWT Authentication** with cryptographic signatures.

### Expected Deliverable
1. **Comparison Matrix**: Compare all three approaches across four criteria:
   - Horizontal scaling elasticity and node failure behavior.
   - Session revocation latency (e.g., instant admin force-logout or single-active-device policy).
   - Infrastructure complexity and operational dependencies (e.g., Redis cluster uptime).
   - Per-request network payload and bandwidth overhead.
2. **Cryptographic & Revocation Analysis**:
   - Explain the difference between JWT integrity verification and data confidentiality/encryption.
   - Propose a viable mechanism for supporting immediate token invalidation/revocation in a JWT-based system without losing all benefits of statelessness.

---

## Exercise: api-tech-selection - Architectural API Paradigm Selection

### Context
An online travel booking system needs architecture decisions for three communication boundaries:
1. **Boundary A: Mobile & Web Consumer Client**: Composite dashboard fetching user profile, flight status, hotel bookings, and reward points over variable-latency mobile networks.
2. **Boundary B: Public Partner Developer API**: External travel agencies and third-party developers querying catalogs and booking reservations.
3. **Boundary C: Internal Inter-Service Communication**: High-throughput, low-latency RPC communication between the Booking Engine, Inventory Service, and Payment Processor.

### Requirements & Constraints
Select the most appropriate API style (`REST`, `GraphQL`, or `gRPC`) for each boundary based on the trade-offs covered in API design foundations.

### Expected Deliverable
A decision table and architectural justification structured as:
- `Boundary` | `Recommended Technology` | `Primary Justification` | `Key Trade-off / Mitigated Risk`
- For each selection, justify based on:
  - Over-fetching / under-fetching and round-trip efficiency.
  - Caching characteristics (e.g., HTTP CDN caching vs application-level caching).
  - Protocol and transport efficiency (e.g., JSON text over HTTP/1.1 vs Protocol Buffers over HTTP/2).
  - Ecosystem tooling, schema contracts, and client integration friction.
