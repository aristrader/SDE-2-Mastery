# API Technologies Study Notes — Complete Conversation Dump
# Part 3/3 — Protobuf Deep Dive, Contracts, Generated Code, Versioning, Drawbacks, Real-World Usage

This continues directly from Part 2.

---

# Protobuf (Protocol Buffers)

---

# User Confusion

> I still don't understand protobuf.
>
> Show where it comes into picture.
>
> Show data transformation.
>
> Show what is actually being saved.

---

# First Important Clarification

Protobuf and gRPC are different things.

---

## Protobuf

Answers:

```text
How should data be represented?
```

Like:

```text
JSON
XML
Protobuf
```

---

## gRPC

Answers:

```text
How should services communicate?
```

---

# Mental Model

REST stack:

```text
HTTP
 +
JSON
```

---

gRPC stack:

```text
HTTP/2
 +
Protobuf
 +
Generated Code
```

---

# User Misconception

Misconception:

> Protobuf removes DTOs.

---

# Correction

No.

DTOs still exist.

Difference:

Without protobuf:

```java
class UserDTO {
    Long id;
    String name;
}
```

You write it manually.

---

With protobuf:

Compiler generates DTO-like classes.

You still work with objects.

You still call:

```java
user.getName()
```

---

# Protobuf Definition Example

```proto
message User {
  int32 id = 1;
  string name = 2;
}
```

---

# What Protobuf Really Gives

Think:

```text
JSON Schema
+
DTO Definition
+
Code Generation
```

combined.

---

# User Objection

> This doesn't look like it saves much.
>
> 1=123 and 2=Swapnil still sends data.

---

# Correction

Tiny examples hide the benefit.

---

# JSON Example

```json
{
  "id":123,
  "name":"Swapnil"
}
```

Every request transmits:

```text
"id"
"name"
```

again.

---

# Larger Example

```json
{
  "id":123,
  "firstName":"Swapnil",
  "lastName":"Agarwal",
  "email":"x@gmail.com",
  "city":"Bangalore",
  "country":"India",
  "phone":"999999999"
}
```

Every request repeatedly sends:

```text
firstName
lastName
email
city
country
phone
```

---

At:

```text
100,000 requests/sec
```

that becomes significant.

---

# Protobuf Equivalent

```proto
message User {
  int64 id = 1;
  string firstName = 2;
  string lastName = 3;
  string email = 4;
  string city = 5;
  string country = 6;
  string phone = 7;
}
```

Runtime transmission only sends field numbers and values.

Field names are not transmitted.

---

# Serialization Comparison

---

## JSON

Object:

```java
User(
  id=123,
  name="Swapnil"
)
```

Serialized:

```json
{
  "id":123,
  "name":"Swapnil"
}
```

Actual text characters transmitted.

---

## Protobuf

Object:

```java
User(
  id=123,
  name="Swapnil"
)
```

Serialized into binary bytes.

Conceptually:

```text
1=123
2=Swapnil
```

Actually transmitted as compact binary.

Something like:

```text
08 7B 12 07 ...
```

(not human readable)

---

# Important Realization

Nobody adopts protobuf because:

```text
One request saved 20 bytes.
```

---

They adopt it because:

```text
20 bytes
×
Millions/Billions of requests
```

becomes meaningful.

---

# Bigger Benefits Than Size

Surprising insight:

Often the biggest benefits are:

```text
Strong Contracts
Code Generation
Cross-Language Support
HTTP/2
Type Safety
```

rather than raw size reduction.

---

# Shared Contract Problem

This became one of the longest confusion threads.

---

# User Question

> If Service A sends:
>
> 1=Swapnil
> 2=26
>
> How does Service B know:
>
> 1=name
> 2=age
>
> Is there a handshake?

---

# Misconception

Misconception:

> Services exchange schema during runtime.

---

# Correction

Usually:

```text
No runtime exchange.
```

---

# Build-Time Sharing

Contract:

```proto
message User {
  string name = 1;
  int32 age = 2;
}
```

Stored in:

```text
contracts-repo/
```

Example:

```text
contracts-repo
 ├── user.proto
 ├── payment.proto
 └── order.proto
```

---

# What Happens Next

Both services import:

```text
contracts-repo
```

during build.

---

Example:

```text
Service A
```

runs:

```text
protoc
```

Generates:

```java
User.java
```

---

Example:

```text
Service B
```

runs:

```text
protoc
```

Generates:

```java
User.java
```

---

Both services now know:

```text
1 -> name
2 -> age
```

before runtime starts.

---

# Important Conclusion

Contract sharing happens at:

```text
Build Time
```

not

```text
Runtime
```

---

# User Follow-Up

> So it's basically like a shared library?

---

# Correction

Yes.

That is actually the closest real-world mental model.

---

Think:

```text
company-contracts-v1.2.jar
```

containing:

```proto
user.proto
payment.proto
```

---

Services depend on it.

During build:

```text
protoc
```

generates language-specific code.

---

# Java Analogy

Without protobuf:

```java
public class UserDTO {
   String name;
   Integer age;
}
```

inside:

```text
common-library.jar
```

shared by services.

---

Protobuf is conceptually:

```text
Shared DTO Library
+
Code Generation
+
Serialization Rules
+
Versioning Support
```

---

# User Question

> If code is generated at build time,
>
> how can I write:
>
> user.getName()
>
> while coding?

---

# Misconception

Misconception:

> Generated class doesn't exist when writing code.

---

# Correction

Generated sources are visible to IDE.

---

Example:

Proto:

```proto
message User {
   string name = 1;
   int32 age = 2;
}
```

Generated:

```java
User
```

---

Developer writes:

```java
User user = response.getUser();

String name = user.getName();

int age = user.getAge();
```

Exactly like normal DTOs.

---

# Builder Pattern Example

Generated classes commonly look like:

```java
User user =
  User.newBuilder()
      .setName("Swapnil")
      .setAge(26)
      .build();
```

---

Reading:

```java
user.getName();
user.getAge();
```

---

# Where Does Mapping Live?

---

# User Question

> Where is:
>
> 1 -> name
> 2 -> age
>
> actually stored?

---

# Correction

Inside generated code.

Conceptually:

```java
switch(fieldNumber) {

   case 1:
      name = readString();
      break;

   case 2:
      age = readInt();
      break;
}
```

Generated automatically.

Developer never writes this.

---

# Important Realization

Application developers usually never think about:

```text
1 -> name
2 -> age
```

They think:

```java
user.getName()
```

just like normal DTOs.

---

# Versioning

Another major discussion.

---

# Version 1

```proto
message User {
  string name = 1;
  int32 age = 2;
}
```

---

Serialized:

```text
1=Swapnil
2=26
```

---

# Version 2

Business adds education.

```proto
message User {
  string name = 1;
  int32 age = 2;
  string education = 3;
}
```

---

Serialized:

```text
1=Swapnil
2=26
3=BTech
```

---

# Old Consumer

Knows:

```text
1=name
2=age
```

Never heard of:

```text
3=education
```

---

Result:

```text
Field 3 ignored.
```

No crash.

No failure.

---

# New Consumer Reading Old Data

Receives:

```text
1=Swapnil
2=26
```

Expected:

```text
1=name
2=age
3=education
```

---

Result:

```text
education=""
```

(default value)

Works.

---

# Major Rule

Never reuse field numbers.

---

Bad:

```proto
string name = 1;
```

Later:

```proto
string education = 1;
```

Disaster.

---

Old message:

```text
1=Swapnil
```

would suddenly become:

```text
education=Swapnil
```

---

# Mental Model

Field numbers are like:

```text
Database Column IDs
```

Keep them stable forever.

---

# Why Not Use Protobuf Everywhere?

This became the final discussion.

---

# User Question

> If protobuf is so good,
>
> why doesn't everyone use it?

---

# Correction

Because every technology has tradeoffs.

---

# Drawback 1: Not Human Readable

JSON:

```json
{
  "name":"Swapnil"
}
```

Easy.

---

Protobuf:

```text
08 1A 12 ...
```

Looks like garbage.

---

Debugging becomes harder.

---

# Example

REST:

```bash
curl /users/1
```

Response immediately understandable.

---

gRPC:

Need tools like:

```text
grpcurl
```

or protobuf-aware tooling.

---

# Drawback 2: Browser Support

Browsers naturally understand:

```text
HTTP
JSON
```

---

JavaScript:

```javascript
fetch(...)
```

works immediately.

---

gRPC/protobuf integration is more complicated.

---

# Drawback 3: Schema Management

Need discipline.

Need versioning.

Need contract ownership.

Need field-number management.

---

JSON often allows looser evolution.

---

# Drawback 4: Build Complexity

Need:

```text
.proto files
protoc
plugins
generated code
contract repos
```

More moving parts.

---

# Drawback 5: Tighter Coupling

REST:

Client knows:

```text
URL
JSON
```

---

gRPC:

Client imports:

```text
Generated Contract
Generated Client
Generated Service Definitions
```

More coupling.

---

# Drawback 6: Public APIs Prefer JSON

Third-party developers may use:

```text
Java
Python
Ruby
PHP
Bash
Postman
Browser
```

JSON works everywhere.

---

# Real-World Architecture

Very common architecture:

```text
Mobile/Web
     |
 REST/JSON
     |
API Gateway
     |
--------------------------------
|              |              |
UserSvc      OrderSvc     PaymentSvc
      \         |         /
       gRPC + Protobuf
```

---

# Why This Architecture Exists

External world:

```text
Humans
Browsers
Third Parties
```

Need:

```text
REST + JSON
```

---

Internal world:

```text
Microservices
High Throughput
Machine-to-Machine
```

Need:

```text
gRPC + Protobuf
```

---

# One Important Interview Insight

A misconception many candidates have:

---

## Misconception

```text
REST = JSON

gRPC = Binary
```

---

## Deeper Correction

REST is:

```text
Resource-Oriented
```

Examples:

```http
/users/123
/orders/456
```

---

gRPC is:

```text
Procedure-Oriented
```

Examples:

```java
getUser()
createOrder()
validatePayment()
```

---

This distinction is more important than:

```text
JSON vs Binary
```

in many interviews.

---

# Final Mental Models

---

## REST

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

## GraphQL

```text
Client Chooses Data Shape
+
Resolvers
+
Single Query
```

---

## gRPC

```text
HTTP/2
+
Protobuf
+
Generated DTOs
+
Generated Clients
+
RPC Methods
```

---

## Sessions

```text
State Stored On Server

Easy Logout
Easy Revocation

Needs Storage
```

---

## JWT

```text
State Carried By Client

Easy Scaling

Hard Revocation
```

---

# Compact Binary Encoding & Varints

Another common discussion: "If everything becomes bytes anyway, why is protobuf faster?"

## JSON Serialization
JSON is text-based. For `{"age": 256}`, it sends characters: `'2', '5', '6'`. Each character is 1 byte, taking 3 bytes total. It also repeatedly transmits field names (`"age"`), colons, quotes, and braces. The JSON parser must match strings, convert text to numbers, and build objects.

**Misconception**: JSON first converts characters to ASCII, and Protobuf skips this stage.
**Correction**: ASCII values are already bytes (e.g. 'A' is 65). Protobuf's savings come from not sending field names and using binary encoding for values, not from bypassing an imaginary "ASCII conversion" stage.

## Protocol Buffers Serialization
Protobuf treats `256` as a numeric value, not as characters. Because the schema is known (`1 = age`), it only transmits `Field=1, Value=256` in binary. 

### Varints (Variable-Length Integers)
A normal 32-bit integer uses 4 bytes, even for small numbers like 5. Protobuf uses **Varint Encoding** to avoid wasting bytes.
- The value `5` fits in 1 byte in Protobuf, but would be 4 bytes in a regular int32, or 1 byte in JSON.
- The value `256` fits in 2 bytes in Protobuf, 4 bytes in regular int32, and 3 bytes in JSON.

**Strings**: Strings don't get magic character compression. The major win for strings is dropping the repeated field names, brackets, and quotes. The actual string content is still transmitted.

---

# Most Important Takeaways From The Entire Conversation

```text
REST
=
Resource-Oriented

GraphQL
=
Client Controls Response Shape

gRPC
=
Procedure-Oriented

Protobuf
=
Shared Contract Compiled Into Code

Sessions
=
Server Stores State

JWT
=
Client Carries State
```

---

# Misconceptions & Corrections Consolidated

### Misconception

REST means no login/session can exist.

### Correction

Authentication is fine.

Question is where state is stored.

---

### Misconception

JWT contains hash of every request.

### Correction

JWT signature protects Header + Payload.

---

### Misconception

JWT is encrypted.

### Correction

Usually signed, not encrypted.

---

### Misconception

Session theft is safer than JWT theft.

### Correction

Both are bearer credentials.

Difference is revocation.

---

### Misconception

One GraphQL field means one DB query.

### Correction

Resolvers can fetch multiple fields together.

---

### Misconception

N+1 means GraphQL is bad.

### Correction

N+1 means resolver implementation is naive.

---

### Misconception

Protobuf removes DTOs.

### Correction

DTOs still exist.

Generated automatically.

---

### Misconception

Services exchange protobuf schema at runtime.

### Correction

Contracts are typically shared at build time.

---

### Misconception

Generated code doesn't exist while coding.

### Correction

IDE sees generated sources.

Use them like normal classes.

---

### Misconception

Protobuf is always better.

### Correction

JSON optimizes for humans.

Protobuf optimizes for machines.

Different tradeoffs.

---

# End of Complete Conversation Dump