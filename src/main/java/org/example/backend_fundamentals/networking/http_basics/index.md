---
order: 40
---

# HTTP Fundamentals

## How it works (SDE2-Level Definition)
HTTP is a stateless application-layer protocol used for communication between a client and server. It follows a request-response model where clients send requests (GET, POST, PUT, DELETE) and servers return responses containing status codes, headers, and optionally a body.

### Why Stateless?
Request 1 and Request 2 are completely independent. The server does not automatically remember who you are or what you did previously. State is usually carried through Cookies, Session IDs, or JWTs.

### What HTTP Defines
HTTP acts as a rulebook for the web. It defines:
- **Methods**: GET, POST, PUT, PATCH, DELETE
- **Headers**: Authorization, Content-Type, etc.
- **Status Codes**: 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 404 Not Found, 500 Internal Server Error
- **Request Format**: Includes method, URL, headers, and optional body.
- **Response Format**: Includes status code, headers, and optional body.

### Request & Response Message Shape
Both HTTP requests and responses share a structured 4-part layout: start/status line, headers, an empty CRLF line separator, and an optional body.

**HTTP/1.1 Request:**
```http
POST /api/v1/orders HTTP/1.1
Host: api.example.com
Content-Type: application/json
Content-Length: 25

{"item_id": 42, "qty": 1}
```

**HTTP/1.1 Response:**
```http
HTTP/1.1 201 Created
Content-Type: application/json
Content-Length: 34

{"order_id": 1001, "status": "ok"}
```

### Method Contracts: Safe vs Idempotent
- **Safe Methods** (read-only semantics; must not alter server resource state): `GET`, `HEAD`, `OPTIONS`, `TRACE`. Safe methods are inherently idempotent.
- **Idempotent Methods** (intended side effect of N identical requests equals that of 1 request): `GET`, `HEAD`, `PUT`, `DELETE`, `OPTIONS`, `TRACE` (per RFC 9110). `POST` and `PATCH` are non-idempotent by default.
- **Retry Policy Impact**: Safe and idempotent requests can be automatically retried by clients or proxies upon network drops or transient 5xx errors without risking duplicate side effects (e.g., duplicate charges vs replacing a resource state with `PUT`). Non-idempotent requests (`POST`) require explicit application-level deduplication (such as idempotency keys) before retrying safely.

## HTTPS Handshake
HTTPS is HTTP running over TLS, providing encryption, integrity, and authentication.

Simplified flow:
1. TCP Connection established
2. TLS Handshake occurs (keys exchanged)
3. Session Key generated
4. Encrypted HTTP Traffic begins

### TLS 1.3 Handshake
In TLS 1.3 (RFC 8446), the handshake normally completes in 1-RTT (one round trip) for a new connection:
1. **ClientHello & ServerHello**: The client sends supported cipher suites and an ephemeral key share in `ClientHello`. The server selects parameters, returns its own ephemeral key share in `ServerHello`, and presents its certificate.
2. **Authentication**: The client validates the server's certificate against trusted Certificate Authorities (CAs), checks hostname matching, and verifies certificate ownership proof.
3. **Session Key & Forward Secrecy**: Both endpoints derive matching symmetric session keys to encrypt subsequent HTTP traffic. Because ephemeral key pairs are used, the session achieves forward secrecy.

## Misconceptions / Gotchas

- **"GET requests cannot have bodies."** The HTTP specification does not strictly forbid it, but almost nobody uses it and many servers/proxies will drop the body.
- **"HTTP, HTTPS, WebSocket, gRPC are at the same level as TCP."** HTTP/HTTPS are application protocols, WebSocket starts with an HTTP Upgrade then uses WebSocket framing, gRPC normally uses HTTP/2, and HTTP/3 uses QUIC over UDP.

## Practical SDE2 checks

- Persistent connections/HTTP/2 multiplexing reduce connection setup overhead.
- ETag plus `If-None-Match` enables a 304 response without retransmitting an unchanged body.
- CORS is browser-enforced and non-simple cross-origin requests can require an OPTIONS preflight.

## Quick recall

**Q. Why is HTTP called stateless?**
A. Because each request is independent. The server does not remember previous requests. State must be carried via cookies, session IDs, or tokens.

**Q. What is HTTPS?**
A. HTTPS is HTTP running over TLS, providing encryption, integrity, and authentication.

**Q. Can a GET request have a body?**
A. Technically the spec doesn't forbid it, but it is heavily discouraged and many systems will ignore or reject it.
