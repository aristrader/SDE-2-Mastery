---
order: 50
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

## HTTPS Handshake
HTTPS is HTTP running over TLS, providing encryption, integrity, and authentication.

Simplified flow:
1. TCP Connection established
2. TLS Handshake occurs (keys exchanged)
3. Session Key generated
4. Encrypted HTTP Traffic begins

## Misconceptions / Gotchas

- **"GET requests cannot have bodies."** The HTTP specification does not strictly forbid it, but almost nobody uses it and many servers/proxies will drop the body.
- **"HTTP, HTTPS, WebSocket, gRPC are at the same level as TCP."** No, they are Application-layer protocols that run *on top* of the Transport-layer protocol (TCP).

## Quick recall

**Q. Why is HTTP called stateless?**
A. Because each request is independent. The server does not remember previous requests. State must be carried via cookies, session IDs, or tokens.

**Q. What is HTTPS?**
A. HTTPS is HTTP running over TLS, providing encryption, integrity, and authentication.

**Q. Can a GET request have a body?**
A. Technically the spec doesn't forbid it, but it is heavily discouraged and many systems will ignore or reject it.


<ExerciseNav />
