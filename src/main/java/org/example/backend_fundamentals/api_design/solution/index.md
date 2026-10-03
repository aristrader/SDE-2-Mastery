---
order: 20
search: false
---

# API Design Solutions

Comprehensive, interview-ready solutions for REST resource and status code modeling, stateful session vs JWT scaling architectures, and API paradigm selection boundaries.

---

## Solution: rest-resource-status-idempotency - REST Contract & Semantics Design

### 1. REST Endpoint Contract Matrix

| Operation | HTTP Verb | URI Path | Safe | Idempotent | Success Status | Key Error Statuses |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **List Orders** | `GET` | `/orders?status=active&page=1&limit=20` | Yes | Yes | `200 OK` | `400`, `401`, `403` |
| **Get Order by ID** | `GET` | `/orders/{orderId}` | Yes | Yes | `200 OK` | `401`, `403`, `404` |
| **Create Draft Order** | `POST` | `/orders` | No | No | `201 Created` (`Location` header) | `400`, `401`, `409`, `422` |
| **Update Delivery Address** | `PATCH` | `/orders/{orderId}/delivery-address` | No | No* | `200 OK` (or `204 No Content`) | `400`, `401`, `403`, `404`, `422` |
| **Replace Delivery Profile** | `PUT` | `/orders/{orderId}/delivery-profile` | No | Yes | `200 OK` (or `204 No Content`) | `400`, `401`, `403`, `404`, `422` |
| **Process Payment** | `POST` | `/orders/{orderId}/payments` | No | No (Requires Key) | `201 Created` (or `200 OK` / `202 Accepted`) | `400`, `401`, `402`, `409`, `422`, `500` |
| **Cancel Unpaid Order** | `DELETE` / `POST` | `/orders/{orderId}` or `/orders/{orderId}/cancellation` | No | Yes (`DELETE`) | `204 No Content` / `200 OK` | `401`, `403`, `404`, `409` (conflict if paid) |

*Note on PATCH idempotency: RFC 5789 defines `PATCH` as neither safe nor idempotent by default because patch formats (such as JSON Patch append operations) can modify state cumulatively, even though an idempotent patch payload can be designed.*

---

### 2. Idempotency Key Specification for Payment Retries

To prevent duplicate charges on client retries (e.g., following network drops, client timeouts, or `503 Service Unavailable` responses):

1. **Client Header Generation**: The client generates a unique UUIDv4 token passed via header: `Idempotency-Key: <uuid>`.
2. **Atomic Lock & State Lookup**: Upon receiving the request, the server executes an atomic `SET key lock NX EX 120` in a fast transactional store (e.g., Redis):
   - **New Key**: Insert a record with status `PROCESSING`. Proceed with payment gateway execution.
   - **In-Flight Duplicate**: If the status is `PROCESSING`, reject subsequent concurrent requests with `409 Conflict` or `425 Too Early` (advising the client that a request is already executing).
3. **Persist Execution Result**: Once the transaction finishes, store the HTTP status code, headers, and serialized response body under the idempotency key with a defined TTL (e.g., 24 hours).
4. **Cached Replay**: Any subsequent request bearing the same key within the TTL immediately bypasses payment gateway execution and returns the cached response with an added header `Idempotency-Replayed: true`.
5. **Payload Fingerprint Verification**: Hash the request payload (`SHA-256(body)`). If an existing key arrives with a mismatched payload hash, return `422 Unprocessable Entity` or `400 Bad Request` to catch key reuse collisions.

---

## Solution: session-vs-jwt-scaling - Stateful Session Scaling vs JWT Trade-Offs

### 1. Scaling Architecture Comparison Matrix

| Architectural Dimension | Sticky Sessions (Session Affinity) | Distributed Shared Session (Redis) | Stateless JWT Authentication |
| :--- | :--- | :--- | :--- |
| **Scaling Elasticity & Node Failures** | **Poor**: Node termination or auto-scaling down drops all sessions pinned to that node, forcing users to re-login. | **High**: Application nodes remain stateless; any instance can handle any request. Node failure causes zero session loss. | **Maximum**: Fully decoupled from server state. Instances scale out or in instantly without session coordination. |
| **Revocation Latency** | **Instant**: Server local in-memory session invalidation drops session immediately. | **Instant**: Deleting or setting TTL=0 on `session:<id>` in Redis invalidates the session globally across all nodes. | **Delayed / Complex**: Valid tokens remain accepted until their expiration (`exp`) claim expires, unless a blocklist is maintained. |
| **Infrastructure & Complexity** | **Low**: No external store required, but requires Layer-7 LB state management (cookies/IP hashing) and risks uneven load. | **Moderate/High**: Requires high-availability Redis cluster (Sentinel/Cluster), connection pooling, monitoring, and failover management. | **Minimal**: Zero centralized database roundtrips per auth check; requires only secure public/private key distribution. |
| **Bandwidth & Payload Overhead** | **Minimal**: Tiny session identifier cookie (e.g., 32-byte `JSESSIONID`). | **Minimal**: Tiny session identifier cookie passed in request headers. | **Moderate**: 500B – 2KB+ base64-encoded string sent in `Authorization: Bearer <token>` on every single HTTP request. |

---

### 2. Cryptographic Integrity vs Data Confidentiality

- **JWT Integrity (RFC 7519 / JWS RFC 7515)**:
  - Standard signed JWTs (JWS) are **signed, not encrypted**.
  - The signature (e.g., HMAC-SHA256 with symmetric secret, or RSA/ECDSA asymmetric keypair) verifies that the payload has **not been tampered with** and confirms the identity of the issuer.
  - The payload is only Base64URL-encoded. Anyone who intercepts the token can decode and view all claims.
  - **Rule**: Sensitive plain data (PII, passwords, internal API keys) must **never** be placed inside standard JWT claims unless encrypted via JWE (JSON Web Encryption, RFC 7516).

---

### 3. Practical Revocation Pattern in JWT Architectures

To preserve stateless verification performance while supporting immediate revocation (e.g., password resets, fraud detection, remote logout):

1. **Short-Lived Access Token + Long-Lived Refresh Token**:
   - Issue signed Access Tokens with short expiration times ($T_{access} = 5\text{–}15\text{ minutes}$).
   - Issue cryptographically random Refresh Tokens ($T_{refresh} = 7\text{–}30\text{ days}$) stored in the persistent database/Redis with token family rotation.
2. **Hybrid Revocation Blocklist / Token Epoch**:
   - Rather than storing every valid JWT, store only **revoked access token IDs (`jti`)** or a **User Token Epoch (`auth_time` / `token_version`)** in an in-memory store (Redis/local cache).
   - On security events (e.g., password change), bump `user.token_version = token_version + 1` or write the `jti` to Redis with a TTL matching the remaining token lifetime ($TTL \le 15\text{ min}$).
   - The token verification filter checks the local cache/Redis only if needed, keeping memory overhead minimal and self-cleaning.

---

## Solution: api-tech-selection - Architectural API Paradigm Selection

### 1. Paradigm Selection Matrix

| Boundary | Recommended Paradigm | Primary Justification | Key Trade-off / Mitigated Risk |
| :--- | :--- | :--- | :--- |
| **Boundary A: Mobile & Web Consumer Client** | **GraphQL** (or Backend-For-Frontend REST) | Prevents mobile over-fetching and resolves under-fetching by aggregating user profile, flights, hotels, and reward points into a single round-trip query. | **Trade-off**: Higher server-side query complexity (N+1 query risks requiring DataLoader) and loss of transparent edge HTTP CDN caching. |
| **Boundary B: Public Partner Developer API** | **REST (OpenAPI / JSON)** | Standardized, ubiquitous HTTP semantics (`GET`, `POST`, status codes), simple client SDK integration, and seamless HTTP/CDN edge caching. | **Trade-off**: Potential slight payload over-fetching; mitigated by targeted resource design and sparse fieldsets (`?fields=id,name,price`). |
| **Boundary C: Internal Inter-Service RPC** | **gRPC (HTTP/2 + Protocol Buffers)** | Ultra-low latency, binary serialization efficiency, compact CPU/network footprint, multiplexed connections, and strict schema enforcement. | **Trade-off**: Requires HTTP/2 support across service mesh/load balancers and lacks human-readable browser/curl debuggability. |

---

### 2. Deep-Dive Trade-off Analysis

1. **Over-fetching / Under-fetching & Round-Trip Efficiency**:
   - **Boundary A (GraphQL)**: Mobile clients on cellular networks suffer when executing sequential cascading REST requests (fetch user $\rightarrow$ fetch bookings $\rightarrow$ fetch points). GraphQL allows declarative nested querying in a single HTTP request payload.
2. **Caching Characteristics**:
   - **Boundary B (REST)**: External third-party travel catalogs exhibit high read-to-write ratios. REST uses standard HTTP headers (`Cache-Control: max-age=300`, `ETag`, `If-None-Match`), allowing intermediaries, public CDNs (Cloudflare, Akamai), and reverse proxies to cache responses transparently. GraphQL queries typically use `POST`, rendering generic HTTP caching ineffective without specialized persisted query extensions.
3. **Protocol & Transport Efficiency**:
   - **Boundary C (gRPC)**: Microservices handle thousands of RPS. JSON over HTTP/1.1 involves expensive string parsing and text framing overhead. gRPC utilizes Protocol Buffers (compact binary encoding) over persistent multiplexed HTTP/2 streams, minimizing socket exhaustion, serialization latency, and packet sizes.
4. **Ecosystem & Integration Friction**:
   - **Boundary B (REST)**: Public developers across varying languages expect OpenAPI specifications (Swagger UI), curl compatibility, and native HTTP client support without requiring specialized client runtimes or compiler tooling.

---

## Quick Recall

- **Safe vs Idempotent**: Safe methods (`GET`, `HEAD`) never mutate server state; idempotent methods (`GET`, `PUT`, `DELETE`, `HEAD`) produce the exact same resulting server state regardless of how many times they are invoked.
- **201 vs 200/204**: Use `201 Created` with a `Location` header when a new resource is created; use `200 OK` when returning content; use `204 No Content` when an update or delete succeeds without returning a body.
- **JWT Security Rule**: Standard JWT (JWS) signatures provide authenticity and tamper protection, not confidentiality; never store unencrypted secrets in JWT claims.
- **Idempotency Key Lifecycle**: Lock on receipt (`PROCESSING`), execute business logic, record response status/body, replay cached response on duplicate keys, reject payload mismatches with `422/400`.
- **API Boundary Rule**: Use GraphQL for heterogeneous composite UI clients; use REST for public developer ecosystems and cacheable resources; use gRPC for high-throughput internal microservice RPCs.
