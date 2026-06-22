# API Technologies: REST, GraphQL, gRPC, Protobuf, JWT, Sessions

This document captures the entire conversation, including explanations, examples, misconceptions, corrections, and follow-up clarifications.

---

# APIs

API = Application Programming Interface.

Purpose:

- Contract between systems.
- Defines how requests are sent.
- Defines how responses are returned.
- Defines data formats.
- Defines error handling.

Example:

Client:

```text
Give me user 123
```

Server:

```json
{
  "id": 123,
  "name": "Swapnil"
}
```

---

# REST

REST = Representational State Transfer.

Core idea:

```text
Everything is a resource.
```

Examples:

```text
/users
/orders
/products
```

REST operations:

```http
GET /users/123
POST /users
PATCH /users/123
DELETE /users/123
```

---

# REST Constraints

## Client-Server

Frontend and backend are independent.

```text
React App
    ↓
REST API
    ↓
Database
```

---

## Stateless

Every request should contain everything needed to process it.

Example:

```http
GET /profile
Authorization: Bearer JWT
```

Server can process request without remembering previous requests.

---

## Cacheable

Responses may be cached.

Example:

```http
GET /products
```

Can be cached for 5 minutes.

---

## Uniform Interface

Consistent URL conventions.

Good:

```text
GET    /users
POST   /users
PATCH  /users/1
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

Existing User:

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

### 301 Permanent Redirect

```http
301 Moved Permanently
Location: newsite.com
```

Browser automatically redirects.

Search engines update indexes.

---

### 302 Temporary Redirect

```http
302 Found
Location: temporary-page
```

Temporary redirect.

Original URL expected to return later.

---

### 304 Not Modified

Browser asks:

```http
GET /logo.png
If-Modified-Since: yesterday
```

Server:

```http
304 Not Modified
```

Browser uses local cache.

No image sent.

---

## 4xx

Client errors.

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

```text
500 Internal Server Error
503 Service Unavailable
```

---

# REST Problems

## Over-Fetching

Resource:

```json
{
  "id":1,
  "name":"Swapnil",
  "email":"...",
  "phone":"..."
}
```

Client only needs:

```json
{
  "name":"Swapnil"
}
```

REST still returns entire resource.

---

## Multiple Requests

Need:

```text
User
Orders
Payments
```

REST:

```http
GET /user/1
GET /orders?user=1
GET /payments?user=1
```

Multiple round trips.

---

# REST Statelessness vs Sessions

## User Confusion

Question:

> REST says stateless. Then what about cookies and sessions? Aren't websites maintaining sessions?

---

## Pure REST

Login:

```http
POST /login
```

Returns:

```json
{
  "token":"abc123"
}
```

Future requests:

```http
GET /profile
Authorization: Bearer abc123
```

Server validates token.

No memory of previous requests required.

Stateless.

---

## Session-Based Authentication

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

Returns:

```http
Set-Cookie: SESSIONID=XYZ
```

Browser stores cookie.

Future request:

```http
GET /profile
Cookie: SESSIONID=XYZ
```

Server looks up:

```text
XYZ -> User123
```

This requires server-side memory.

Not perfectly RESTful.

---

## Misconception

Misconception:

> If sessions are not RESTful, nobody should use them.

Correction:

Real companies use sessions all the time.

Reason:

```text
Practicality > Purity
```

---

## Why Sessions Are Useful

### Force Logout

Delete:

```text
XYZ -> User123
```

User immediately logged out.

---

### Single Device Login

Store:

```text
User123 -> SessionXYZ
```

Delete old session when new login occurs.

---

### Banking Systems

Sometimes banks WANT server control over every session.

---

## JWT Advantages

No session store required.

Request can hit:

```text
App1
App2
App3
App4
```

without shared memory.

Good for scaling.

---

# JWT

## User Misconception

Misconception:

> JWT contains hash of request.

Correction:

JWT contains:

```text
Header
Payload
Signature
```

Not request hashes.

---

## JWT Structure

Example:

```text
xxxxx.yyyyy.zzzzz
```

---

### Header

```json
{
  "alg":"HS256",
  "typ":"JWT"
}
```

---

### Payload

```json
{
  "userId":123,
  "role":"ADMIN",
  "exp":1712345678
}
```

Contains claims.

---

### Signature

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

Stored in token.

---

## Verification

Server receives token.

Recomputes:

```text
HMAC(
 Header + Payload,
 SecretKey
)
```

If signature matches:

```text
Token is valid
```

---

## JWT Is Not Encrypted

Misconception:

> JWT contents are hidden.

Correction:

JWT payload can usually be decoded.

Example:

```json
{
  "userId":123,
  "role":"ADMIN"
}
```

visible.

Signature protects integrity.

Not secrecy.

---

# Symmetric JWT (HS256)

One shared secret.

Creation:

```text
HMAC(data, secret)
```

Verification:

```text
HMAC(data, secret)
```

Same key.

---

# Asymmetric JWT (RS256)

Uses:

```text
Private Key
Public Key
```

---

## Creation

Auth Service:

```text
Sign(
 Header+Payload,
 PrivateKey
)
```

---

## Verification

Order Service:

```text
Verify(
 Header+Payload,
 Signature,
 PublicKey
)
```

---

## User Question

> Where does public key come from?

---

### Answer

Usually:

#### Option 1

Distributed during deployment.

```text
Auth Service
  Private Key

Order Service
  Public Key

Payment Service
  Public Key
```

---

#### Option 2

Auth Service exposes:

```text
/public-key
```

or

```text
/jwks
```

Services fetch once and cache.

Not every request.

---

## Why Not Verify Through Auth Service Every Request?

Because:

```text
Order Service
    ↓
Auth Service
    ↓
Verify
```

would happen on every request.

Problems:

- Extra network calls.
- Auth Service bottleneck.
- Single point of failure.

---

## Passport Analogy

Passport:

```text
Government signs passport
```

Airport officer verifies locally.

Doesn't call government every time.

JWT verification works similarly.

---

# JWT vs Session Security

## User Question

> If someone steals Session ID, isn't it same as stealing JWT?

---

## Answer

Yes.

100%.

---

### Session Theft

Attacker gets:

```text
SESSIONID=ABC123
```

Can send:

```http
Cookie: SESSIONID=ABC123
```

Authenticated.

---

### JWT Theft

Attacker gets:

```http
Authorization: Bearer JWT
```

Authenticated.

---

## Difference

Sessions:

```text
ABC123 -> User123
```

stored server-side.

Delete mapping:

```text
Session invalid immediately.
```

---

JWT:

Token still valid until:

```text
Expiration
```

or

```text
Blacklist
```

or

```text
Revocation system
```

---

## Tradeoff

Sessions:

```text
Easy logout
Easy revocation

Need storage
```

JWT:

```text
Easy scaling
No storage

Hard revocation
```

---

# GraphQL

Created by Facebook.

Core idea:

```text
Client chooses exact data shape.
```

---

# GraphQL Query

```graphql
{
  getUser {
    id
    name
  }
}
```

Returns:

```json
{
  "getUser": {
    "id":123,
    "name":"Swapnil"
  }
}
```

---

# GraphQL Solves Over-Fetching

REST:

```http
GET /user/1
```

Returns everything.

GraphQL:

```graphql
{
  getUser {
    name
  }
}
```

Returns only:

```json
{
  "name":"Swapnil"
}
```

---

# Schema

Example:

```graphql
type User {
  id: ID
  name: String
  city: String
}
```

Defines available fields.

---

# Queries vs Mutations

Read:

```graphql
query {
  getUser {
    name
  }
}
```

Write:

```graphql
mutation {
  createUser(name:"Swapnil")
}
```

---

# Resolvers

Resolver = GraphQL request handler.

---

## User Understanding

Question:

> GraphQL is basically a backend with resolvers that know where data comes from?

Correction:

Yes.

Good mental model.

---

Example:

```graphql
{
  user {
    name
    dob
  }
}
```

Resolver:

```java
getUser()
```

might execute:

```sql
SELECT name,dob
FROM users
```

One DB query.

---

## Misconception

Misconception:

> One field = one resolver = one DB call.

Correction:

Not necessarily.

One resolver may load:

```java
User {
 name,
 dob
}
```

once.

Fields extracted from loaded object.

No extra DB calls.

---

# N+1 Problem

Query:

```graphql
{
 users {
   orders {
      id
   }
 }
}
```

---

Execution:

```sql
SELECT * FROM users
```

returns 100 users.

Then:

```sql
SELECT * FROM orders WHERE user_id=1
SELECT * FROM orders WHERE user_id=2
...
```

100 more queries.

Total:

```text
101 queries
```

---

# Solutions

## Batch Loading

Instead of:

```sql
WHERE user_id=1
WHERE user_id=2
```

Use:

```sql
WHERE user_id IN (...)
```

Common tool:

```text
DataLoader
```

---

## Join

```sql
SELECT *
FROM users
LEFT JOIN orders
```

---

## Eager Loading

Examples:

```java
JOIN FETCH
```

or

```java
@EntityGraph
```

---

## Caching

Can help reduce repeated queries.

---

# gRPC

Core idea:

```text
Remote Procedure Call
```

Looks like local method invocation.

---

REST:

```http
GET /users/123
```

---

gRPC:

```java
userService.getUser(123);
```

---

# Protocol Buffers

## Initial User Confusion

Question:

> I still don't understand protobuf.

---

## Key Realization

Protobuf is:

```text
Data Format
```

Like:

```text
JSON
XML
```

---

gRPC is:

```text
Communication Framework
```

---

Relationship:

```text
JSON : REST

Protobuf : gRPC
```

---

# Proto Definition

```proto
message User {
  int32 id = 1;
  string name = 2;
}
```

---

# Misconception

Misconception:

> Protobuf eliminates DTOs.

Correction:

No.

DTOs still exist.

Generated automatically.

---

Without protobuf:

```java
class UserDTO {
  Long id;
  String name;
}
```

---

With protobuf:

Compiler generates:

```java
User
```

class.

---

# Serialization Difference

JSON:

```json
{
  "id":123,
  "name":"Swapnil"
}
```

Sends field names every request.

---

Protobuf:

Conceptually:

```text
1=123
2=Swapnil
```

Field names not transmitted.

---

# Real Benefit

Not:

```text
Save 20 bytes once
```

But:

```text
20 bytes
×
1 billion requests
```

---

# HTTP/2 Benefit

gRPC uses:

```text
HTTP/2
```

Supports:

```text
Multiplexing
Streaming
Header Compression
```

---

# Shared Contract

## User Question

> How do both services know field mappings?

---

## Misconception

Misconception:

> Services exchange schema at runtime.

Correction:

Usually no runtime exchange.

---

Build-time sharing.

Example:

```text
contracts-repo

user.proto
payment.proto
```

Shared dependency.

---

Build:

```text
protoc
```

generates:

```java
User.java
```

for Java.

```go
User.go
```

for Go.

---

Both services compile against same contract.

---

# Versioning

Version 1:

```proto
message User {
  string name = 1;
  int32 age = 2;
}
```

---

Version 2:

```proto
message User {
  string name = 1;
  int32 age = 2;
  string education = 3;
}
```

---

Old service receives:

```text
1=Swapnil
2=26
3=BTech
```

Doesn't know field 3.

Ignores it.

Works.

---

New service receives:

```text
1=Swapnil
2=26
```

Field 3 missing.

Gets default value.

Works.

---

## Golden Rule

Never reuse field numbers.

Bad:

```proto
name = 1
```

later becomes:

```proto
education = 1
```

Dangerous.

---

# How Developers Actually Use Generated Classes

User confusion:

> If classes are generated at build time, how can I write code?

---

Answer:

Generated classes become available in IDE.

You write:

```java
String name = user.getName();
```

Exactly like a normal DTO.

---

Example:

```java
User user =
  User.newBuilder()
      .setName("Swapnil")
      .setAge(26)
      .build();
```

Read:

```java
user.getName()
user.getAge()
```

---

Field-number mapping is hidden inside generated serialization/deserialization code.

Application developers rarely interact with field numbers.

---

# Why Not Use Protobuf Everywhere?

## Misconception

Misconception:

> Protobuf is better, so everyone should use it.

Correction:

Every technology has tradeoffs.

---

## Drawback 1

Not human readable.

JSON:

```json
{
  "name":"Swapnil"
}
```

Easy to inspect.

---

Protobuf:

```text
08 1A 12 ...
```

Not readable.

---

## Drawback 2

Browser Support

Browsers naturally understand:

```text
HTTP + JSON
```

Not protobuf/gRPC.

---

## Drawback 3

Schema Management

Need:

```text
.proto
versioning
field discipline
```

---

## Drawback 4

Build Complexity

Need:

```text
protoc
plugins
generated code
contract repos
```

---

## Drawback 5

Tighter Coupling

REST:

Client knows URL and JSON.

---

gRPC:

Client imports generated contract.

Stronger coupling.

---

## Drawback 6

Public APIs Prefer JSON

Third-party developers can:

```bash
curl
```

and immediately understand response.

---

# Real-World Architecture

Very common:

```text
Browser / Mobile
        |
 REST / JSON
        |
API Gateway
        |
--------------------------------
|              |              |
UserSvc      OrderSvc     PaymentSvc
      \         |         /
       gRPC + Protobuf
```

External:

```text
REST + JSON
```

Internal:

```text
gRPC + Protobuf
```

---

# Final Mental Models

REST:

```text
HTTP
+
JSON
+
Manual DTOs
+
Resource URLs
```

---

gRPC:

```text
HTTP/2
+
Protobuf
+
Generated DTOs
+
Generated Clients
+
RPC Calls
```

---

Most Important Summary

```text
JSON
=
Send schema + data
every request

Protobuf
=
Share schema once
during development

Send only data
during runtime
```

and

```text
A .proto file is essentially a language-neutral shared DTO contract that all services compile into their own language-specific classes.
```