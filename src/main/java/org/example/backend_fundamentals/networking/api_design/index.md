---
order: 10
---

# API Technologies Study Notes — Complete Conversation Dump
# Part 2/3 — JWT Deep Dive, Session vs JWT Security, GraphQL, N+1, gRPC, Protobuf Foundations

This continues directly from Part 1.

---

# JWT Symmetric vs Asymmetric Signing

---

# User Question

> I remember something about public/private keys.
>
> How does JWT verification actually work?

---

# Symmetric JWT (HS256)

One shared secret.

Creation:

```text
HMAC(
 Header + Payload,
 SecretKey
)
```

Verification:

```text
HMAC(
 Header + Payload,
 SecretKey
)
```

Same key used for both.

---

# Mental Model

```text
Server
  |
SecretKey
```

Only server knows secret.

Server creates token.

Server verifies token.

Simple.

---

# Asymmetric JWT (RS256)

Uses:

```text
Private Key
Public Key
```

---

# Token Creation

Auth Service owns:

```text
Private Key
```

Creates signature:

```text
Sign(
 Header + Payload,
 PrivateKey
)
```

Returns JWT.

---

# Token Verification

Services have:

```text
Public Key
```

Verify:

```text
Verify(
 Header + Payload,
 Signature,
 PublicKey
)
```

---

# Why Use Public/Private Keys?

Imagine:

```text
Auth Service
Order Service
Payment Service
Inventory Service
```

Only Auth Service should create tokens.

But all services should verify them.

---

With asymmetric crypto:

```text
Auth Service
    |
Private Key
```

Only Auth Service can sign.

---

Other services:

```text
Order Service
Payment Service
Inventory Service
```

have:

```text
Public Key
```

Can verify.

Cannot create fake tokens.

---

# User Question

> Where does public key come from?
>
> Doesn't Order Service need to call Auth Service?

---

# Correction

Usually:

```text
No.
```

That defeats the purpose.

---

# Why Not Verify Through Auth Service Every Request?

Imagine:

```text
Client
  |
Order Service
  |
Auth Service
```

Every request requires:

```text
Verify Token
```

Problems:

- Extra network call.
- More latency.
- Auth Service bottleneck.
- Single point of failure.

---

# How Public Key Is Distributed

## Option 1

Deployment-time configuration.

Example:

```text
Auth Service
  Private Key

Order Service
  Public Key

Payment Service
  Public Key
```

Configured once.

---

## Option 2

Auth Service exposes:

```text
/public-key
```

or

```text
/jwks
```

endpoint.

Services fetch:

```text
Once
```

or

```text
At startup
```

or

```text
Every few hours
```

and cache.

---

# Cost Benefit

Verification becomes:

```text
JWT
  |
Order Service
  |
Verify Locally
```

No network call.

Very fast.

---

# Passport Analogy

Government:

```text
Issues Passport
```

Airport officer:

```text
Verifies Signature
```

Doesn't call government every time.

JWT verification works similarly.

---

# Session vs JWT Security

---

# User Question

> If someone steals Session ID, can't they use it?
>
> Isn't that exactly like stealing JWT?

---

# Correction

Yes.

100%.

This was an important realization.

---

# Session Theft

Attacker steals:

```text
SESSIONID=ABC123
```

Sends:

```http
Cookie: SESSIONID=ABC123
```

Server says:

```text
Yep.
That's User123.
```

Access granted.

---

# JWT Theft

Attacker steals:

```http
Authorization: Bearer JWT
```

Server says:

```text
Signature Valid
```

Access granted.

---

# Important Realization

From a theft perspective:

```text
Stolen SessionID
=
Stolen JWT
```

Both are:

```text
Bearer Credentials
```

Whoever possesses them can use them.

---

# User Follow-up

> Then what's the difference?

---

# Difference = Revocation

---

## Sessions

Stored:

```text
ABC123 -> User101
```

Delete entry:

```text
ABC123 removed
```

Session instantly invalid.

---

## JWT

Server receives:

```text
Valid JWT
```

Checks:

```text
Signature Valid
Expiry Valid
```

Accepts it.

---

To revoke JWT you need:

```text
Blacklist
Revocation Table
Redis Check
```

Extra complexity.

---

# Tradeoff

Sessions:

```text
Easy Logout
Easy Revocation
Easy Control

Need Storage
```

JWT:

```text
Easy Scaling
Stateless

Hard Revocation
```

---

# User Observation

> Hackers won't use web pages.
>
> They'll call APIs directly.

---

# Correction

Correct.

Eventually authentication becomes:

```text
Who possesses credential?
```

not

```text
Who opened browser?
```

---

If attacker gets:

```text
Password
SessionID
JWT
```

they can usually call APIs directly.

---

# Security Measures Mentioned

```text
HTTPS
Secure Cookies
HttpOnly Cookies
Short Expiration
MFA
```

---

# GraphQL

Created by Facebook.

Core idea:

```text
Client decides exact response shape.
```

---

# Why GraphQL Exists

REST:

```http
GET /user/1
```

Returns:

```json
{
  "id":123,
  "name":"Swapnil",
  "email":"...",
  "phone":"..."
}
```

---

Client only needs:

```text
name
```

GraphQL solves that.

---

# Query Example

```graphql
{
  getUser {
    name
  }
}
```

Response:

```json
{
  "getUser": {
    "name":"Swapnil"
  }
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

# Queries

Read operations.

Example:

```graphql
query {
  getUser {
    name
  }
}
```

---

# Mutations

Write operations.

Example:

```graphql
mutation {
  createUser(name:"Swapnil")
}
```

---

# Resolvers

Resolver = GraphQL request handler.

---

# User Understanding

Question:

> GraphQL is basically using backend resolvers that know where data comes from?

---

# Correction

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

could execute:

```sql
SELECT name,dob
FROM users
```

One query.

---

# Important Confusion

---

## Misconception

> If name and DOB are requested, does GraphQL do two fetches?

---

## Correction

No.

If both fields live in same row:

```sql
SELECT name,dob
FROM users
```

One fetch.

No double querying.

---

# Another Example

Query:

```graphql
{
  user {
    name
    orders {
      id
    }
  }
}
```

Maybe:

```text
name -> User DB
orders -> Order Service
```

Now multiple sources involved.

---

# Important Misconception

Misconception:

> One GraphQL field means one DB query.

Correction:

Not necessarily.

Good implementations often:

```java
loadUser()
```

once.

Then:

```java
user.getName()
user.getDob()
```

read from loaded object.

---

# N+1 Problem

One of the most important GraphQL interview topics.

---

# Example

Query:

```graphql
{
  users {
    id
    orders {
      id
    }
  }
}
```

---

Step 1

```sql
SELECT *
FROM users
```

Returns:

```text
User1
User2
...
User100
```

One query.

---

Step 2

For every user:

```sql
SELECT *
FROM orders
WHERE user_id=1
```

```sql
SELECT *
FROM orders
WHERE user_id=2
```

...

100 queries.

---

Total:

```text
1 + 100 = 101 queries
```

Called:

```text
N+1 Problem
```

---

# User Memory

You remembered:

> Batch 50-50 requests?
>
> Fetch list of IDs?

Exactly.

That's one common solution.

---

# Solution 1: Batch Loading

Instead of:

```sql
WHERE user_id=1
WHERE user_id=2
WHERE user_id=3
```

Do:

```sql
SELECT *
FROM orders
WHERE user_id IN (
 1,2,3,...100
)
```

One query.

---

# DataLoader

Common GraphQL tool.

Conceptually:

Application calls:

```java
load(user1)
load(user2)
load(user3)
```

Internally becomes:

```java
load([1,2,3])
```

One DB hit.

---

# Solution 2: Join

```sql
SELECT *
FROM users
LEFT JOIN orders
```

Single query.

---

# Solution 3: Eager Loading

Examples:

```java
JOIN FETCH
```

or

```java
@EntityGraph
```

Load related data up front.

---

# Solution 4: Cache

Redis.

Local cache.

Not primary solution.

Can help.

---

# Important Misconception

Misconception:

> N+1 means GraphQL is bad.

Correction:

N+1 means resolver implementation is bad.

Proper batching solves it.

---

# gRPC Introduction

---

# User Confusion

> I still don't understand gRPC.
>
> Start from layman terms.

---

# Key Insight

Many people think:

```text
REST vs gRPC
=
JSON vs Binary
```

Not really.

---

The deeper distinction:

REST:

```text
Resource-Oriented
```

gRPC:

```text
Procedure-Oriented
```

---

# REST

Think:

```text
Users
Orders
Payments
```

URLs:

```http
/users/123
/orders/456
```

---

# gRPC

Think:

```java
getUser()
createOrder()
validatePayment()
```

Functions.

---

# REST Example

```http
GET /users/123
```

---

# gRPC Example

```java
userService.getUser(123)
```

Looks like local method call.

---

# Why Google Built It

Imagine:

```text
Order Service
Payment Service
```

communicating:

```text
10,000 requests/sec
```

Problems with REST:

- JSON overhead.
- Manual DTOs.
- Serialization cost.

---

# End of Part 2/3

Part 3 will continue with:

- Protobuf deep dive
- Why field numbers exist
- Shared contracts
- Generated DTOs
- Build-time schema sharing
- Versioning
- Schema evolution
- Generated code examples
- Why protobuf isn't used everywhere
- Real-world architectures
- All protobuf misconceptions and corrections