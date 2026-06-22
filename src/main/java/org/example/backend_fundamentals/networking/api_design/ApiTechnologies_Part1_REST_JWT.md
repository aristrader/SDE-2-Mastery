# API Technologies Study Notes — Complete Conversation Dump
# Part 1/3 — APIs, REST, Statelessness, Sessions, JWT, HTTP Status Codes

This document captures all topics, questions, misconceptions, corrections, examples, and follow-up clarifications discussed in this part of the conversation.

---

# APIs

API = Application Programming Interface.

Purpose:

- Contract between systems.
- Defines how requests are made.
- Defines how responses are returned.
- Defines data formats.
- Defines error handling.

Simple mental model:

```text
Client
   |
API
   |
Server
```

Example:

Client:

```text
Give me user 123
```

Server:

```json
{
  "id":123,
  "name":"Swapnil"
}
```

---

# REST

REST = Representational State Transfer.

Most common API style.

Core idea:

```text
Everything is a resource.
```

Examples:

```text
/users
/orders
/products
/payments
```

---

# REST Endpoints

Get user:

```http
GET /users/123
```

Create user:

```http
POST /users
```

Update user:

```http
PATCH /users/123
```

Delete user:

```http
DELETE /users/123
```

---

# REST Constraints

## Client-Server

Frontend and backend are independent.

Example:

```text
React App
    ↓
REST API
    ↓
Database
```

---

## Stateless

Each request should contain everything required to process it.

Example:

```http
GET /profile
Authorization: Bearer JWT
```

Server should not need information from previous requests.

---

## Cacheable

Responses can be cached.

Example:

```http
GET /products
```

Cache for 5 minutes.

Reduces DB load.

---

## Uniform Interface

Consistent conventions.

Good:

```text
GET /users
POST /users
PATCH /users/1
DELETE /users/1
```

Bad:

```text
/getUsers
/createUser
/removeUser
```

---

# HTTP Verbs

| Verb | Meaning |
|--------|---------|
| GET | Read |
| POST | Create |
| PUT | Replace entire resource |
| PATCH | Partial update |
| DELETE | Delete |
| HEAD | Metadata only |

---

# PUT vs PATCH

Existing user:

```json
{
  "name":"John",
  "age":25
}
```

PUT:

```json
{
  "name":"Bob"
}
```

Result:

```json
{
  "name":"Bob"
}
```

Entire resource replaced.

---

PATCH:

```json
{
  "name":"Bob"
}
```

Result:

```json
{
  "name":"Bob",
  "age":25
}
```

Only specified fields updated.

---

# HTTP Status Codes

## 1xx

Informational.

Rarely discussed in interviews.

---

## 2xx

Success.

Examples:

```text
200 OK
201 Created
204 No Content
```

---

## 3xx

Redirection.

---

### User Question

> Explain 3xx errors.

---

### 301 Moved Permanently

Example:

```http
GET oldsite.com
```

Response:

```http
301 Moved Permanently
Location: newsite.com
```

Browser automatically goes to:

```http
GET newsite.com
```

Search engines update indexes.

Permanent change.

---

### 302 Found / Temporary Redirect

Example:

```http
302 Found
Location: maintenance-page
```

Meaning:

```text
Use another URL temporarily.
```

Original URL may return later.

---

### 304 Not Modified

Important for caching.

Yesterday browser downloaded:

```http
GET /logo.png
```

Today:

```http
GET /logo.png
If-Modified-Since: yesterday
```

Server responds:

```http
304 Not Modified
```

Browser uses cached image.

No image transferred.

Bandwidth saved.

---

## 4xx

Client errors.

Examples:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
429 Too Many Requests
```

---

## 5xx

Server errors.

Examples:

```text
500 Internal Server Error
503 Service Unavailable
```

---

# REST Problems

## Over-Fetching

Suppose:

```json
{
  "id":1,
  "name":"Swapnil",
  "email":"x@gmail.com",
  "phone":"123",
  "address":"..."
}
```

Client only needs:

```json
{
  "name":"Swapnil"
}
```

REST still returns everything.

Called:

```text
Over-Fetching
```

---

## Multiple Round Trips

Need:

```text
User
Orders
Payments
```

REST may require:

```http
GET /user/1
GET /orders?user=1
GET /payments?user=1
```

Three network calls.

---

# REST Statelessness vs Sessions

This became one of the biggest confusion points.

---

# User Confusion

Question:

> REST says stateless.
>
> But websites clearly maintain login sessions.
>
> Cookies exist.
>
> Isn't that state?
>
> Is there some separate protocol?

---

# Correction

Yes.

There is state somewhere.

The question is:

```text
Where is the state stored?
```

---

# Pure REST / Stateless Example

Login:

```http
POST /login
```

Response:

```json
{
  "token":"abc123"
}
```

Future request:

```http
GET /profile
Authorization: Bearer abc123
```

Server validates token.

Returns profile.

Tomorrow:

```http
GET /profile
Authorization: Bearer abc123
```

Same process.

Server doesn't need memory of previous requests.

Stateless.

---

# Session-Based Authentication

Traditional websites often work like this.

Login:

```http
POST /login
```

Server creates:

```text
SessionID = XYZ
```

Stores:

```text
XYZ -> User123
```

in memory, Redis, or DB.

Returns:

```http
Set-Cookie: SESSIONID=XYZ
```

Browser stores cookie.

---

Future request:

```http
GET /profile
Cookie: SESSIONID=XYZ
```

Server executes:

```java
sessionStore.get("XYZ")
```

Finds:

```text
User123
```

Returns profile.

---

# Why This Is Not Pure REST

Request #2 depends on information remembered from request #1.

Server has state:

```text
XYZ -> User123
```

stored somewhere.

REST's stateless constraint says ideally:

```text
Don't do that.
```

---

# Major Misconception

Misconception:

> If sessions aren't perfectly RESTful, companies shouldn't use them.

Correction:

Companies absolutely use them.

A lot.

---

# Why Companies Use Sessions Anyway

Because REST purity isn't the goal.

Business requirements are.

---

# Example: Force Logout

Current session:

```text
ABC123 -> User101
```

Admin clicks:

```text
Logout User
```

Delete:

```text
ABC123
```

User instantly logged out.

---

# Example: Single Login Policy

Store:

```text
User101 -> SessionABC
```

User logs in elsewhere:

Delete old session.

Create new one.

Easy.

---

# Example: Banking

Banks often want server-side control.

They may prefer:

```text
Every request depends on active server-side session.
```

instead of:

```text
Long-lived self-contained JWT.
```

---

# Real-World Conclusion

Many systems are:

```text
REST APIs
+
Session Authentication
```

Technically not pure REST.

Practically very common.

---

# Scaling Problem With Sessions

Suppose:

```text
App1
App2
App3
...
App10
```

User logs into:

```text
App1
```

Session stored in App1 memory.

Next request hits:

```text
App7
```

Problem:

```text
App7 doesn't know session.
```

---

# Solutions

## Sticky Sessions

Load balancer always routes user to App1.

Scales poorly.

---

## Shared Redis

```text
App1
App2
App3
  |
Redis
```

Works.

But Redis becomes critical infrastructure.

---

# Why JWT Became Popular

Request:

```http
Authorization: Bearer JWT
```

can hit:

```text
App1
App2
App3
App4
```

No session lookup required.

No Redis dependency required.

Easy horizontal scaling.

---

# JWT

---

# User Confusion

Question:

> Whoever has JWT is authenticated right?
>
> JWT holds hash of request right?
>
> I forgot how verification works.

---

# JWT Structure

JWT contains:

```text
Header
Payload
Signature
```

Example:

```text
xxxxx.yyyyy.zzzzz
```

Three sections.

---

# Header

Example:

```json
{
  "alg":"HS256",
  "typ":"JWT"
}
```

Meaning:

```text
Algorithm = HS256
Type = JWT
```

---

# Payload

Example:

```json
{
  "sub":"123",
  "role":"ADMIN",
  "exp":1712345678
}
```

Contains claims.

Usually:

```text
User ID
Roles
Permissions
Expiry
```

---

# Signature

Server computes:

```text
HMAC(
 Header + Payload,
 SecretKey
)
```

Result:

```text
ABCXYZ123
```

Stored as signature.

---

# Verification

Client sends:

```text
Header.Payload.Signature
```

Server:

1. Extracts Header.
2. Extracts Payload.
3. Extracts Signature.

Recomputes:

```text
HMAC(
 Header + Payload,
 SecretKey
)
```

If result matches:

```text
Token is valid.
```

---

# Important Correction

Misconception:

> JWT contains hash of request.

Correction:

JWT signature is generated from:

```text
Header + Payload
```

not from every API request.

---

# Important Correction

Misconception:

> JWT is encrypted.

Correction:

JWT is usually NOT encrypted.

Example:

```text
eyJhbGciOi...
```

can often be decoded into:

```json
{
  "userId":123,
  "role":"ADMIN"
}
```

without any secret key.

---

The signature protects:

```text
Integrity
```

not

```text
Secrecy
```

---

# Tampering Example

Original payload:

```json
{
  "userId":123
}
```

Attacker changes:

```json
{
  "userId":999,
  "role":"ADMIN"
}
```

Signature no longer matches.

Server rejects token.

---

# End of Part 1/3

Part 2 will continue with:

- JWT symmetric vs asymmetric
- Public/private key distribution
- Session theft vs JWT theft
- GraphQL
- Resolvers
- Over-fetching
- N+1 problem
- DataLoader
- gRPC introduction
- Protobuf introduction
- All associated misconceptions and corrections