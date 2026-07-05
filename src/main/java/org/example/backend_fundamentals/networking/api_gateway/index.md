---
order: 20
---

# API Gateway

## Definition

An API Gateway is an API management tool that sits between clients and backend services.

Architecture:

```text
Client
   ↓
API Gateway
   ↓
+-------------+
| User        |
| Product     |
| Order       |
| Payment     |
| Review      |
+-------------+
```

It acts as:

- Single entry point
- Reverse proxy
- Routing layer
- Authentication layer
- Rate limiting layer
- Logging layer
- Monitoring layer
- API composition layer

---

# Why API Gateway Exists

## Problem Without API Gateway

Suppose we have:

```text
User Service
Product Service
Order Service
Payment Service
Review Service
```

Without a gateway:

```text
Mobile App
    ├── User Service
    ├── Product Service
    ├── Order Service
    ├── Payment Service
    └── Review Service
```

Frontend must know:

- Every service location
- Every endpoint
- Authentication details
- Retry logic
- Error handling

Example:

To show a product page:

```text
GET /product/123
GET /product/123/reviews
GET /user/profile
GET /inventory/123
```

Frontend makes multiple calls.

---

## With API Gateway

Frontend calls:

```text
api.company.com
```

Gateway handles everything internally.

```text
Client
   ↓
API Gateway
   ↓
Services
```

---

# Hotel Reception Analogy

Without reception:

```text
Guest
  ↓
Laundry
Restaurant
Housekeeping
Maintenance
```

Guest must know every department.

With reception:

```text
Guest
   ↓
Reception
   ↓
Departments
```

Reception routes requests.

API Gateway acts as reception.

---

# Microservices Motivation

Microservices often expose fine-grained APIs.

Example:

Order page requires:

```text
User Service
Order Service
Payment Service
Shipping Service
```

Without gateway:

```text
Frontend
   ↓
4 separate calls
```

With gateway:

```text
Frontend
   ↓
1 request
   ↓
Gateway
   ↓
4 internal calls
```

---

# API Composition

One major responsibility.

Frontend wants:

```json
{
  "user": {},
  "orders": [],
  "cart": {}
}
```

Data exists in:

```text
User Service
Order Service
Cart Service
```

Gateway:

```text
Call User Service
Call Order Service
Call Cart Service
Merge results
Return one response
```

Response:

```json
{
  "user": {...},
  "orders": [...],
  "cart": {...}
}
```

---

# Authentication and Authorization

Without gateway:

Every service validates:

```text
JWT
OAuth Token
Permissions
```

With gateway:

```text
Client
   ↓
Gateway
   ↓
Authentication
   ↓
Services
```

Benefits:

- Centralized security
- Less duplicated code
- Consistent behavior

---

# Reverse Proxy

Important interview point.

API Gateway is essentially an advanced reverse proxy.

Client sees:

```text
api.company.com
```

Gateway routes internally:

```text
/product/* → Product Service
/user/* → User Service
/order/* → Order Service
```

Backend services remain hidden.

---

# Routing

Example:

```text
/api/users/*    → User Service
/api/orders/*   → Order Service
/api/products/* → Product Service
```

Gateway determines destination.

---

# Service Discovery

Question:

> How does gateway know where services live?

Services move.

Example:

```text
Product Service
10.0.1.2
```

Later:

```text
Product Service
10.0.7.8
```

Gateway uses service discovery.

Examples mentioned:

- Eureka
- Consul
- Kubernetes Service Discovery

---

# Load Balancing

Gateway may load balance.

Example:

```text
Gateway
    ↓
Product Service A
Product Service B
Product Service C
```

Traffic distributed across instances.

---

# Caching

Example:

```text
GET /product/123
```

Gateway caches response.

```text
First request
    ↓
Service

Later requests
    ↓
Cache
```

Benefits:

- Lower latency
- Reduced backend load

---

# Rate Limiting

Protects backend systems.

Example:

```text
User A:
10000 requests/minute
```

Gateway may respond:

```text
429 Too Many Requests
```

Algorithms mentioned:

- Token Bucket
- Leaky Bucket
- Fixed Window
- Sliding Window

---

# Throttling

Difference:

Rate limiting:

```text
Reject excess traffic
```

Throttling:

```text
Slow down excess traffic
```

Purpose:

Prevent overload.

---

# Retry

Example:

```text
Gateway
   ↓
Product Service
```

Temporary issue:

```text
Timeout
```

Gateway retries:

```text
Retry #1
Retry #2
Retry #3
```

Improves resilience.

---

# Circuit Breaker (Gateway Feature Mention)

If Product Service is down:

Without breaker:

```text
Gateway
 ↓
Timeout
 ↓
Retry
 ↓
Timeout
```

With breaker:

```text
Service failing repeatedly
```

Gateway opens circuit:

```text
Immediate failure
```

No more requests sent temporarily.

---

# Logging

Gateway sees all traffic.

Examples:

```text
Request logging
Response logging
Error logging
```

Example log:

```text
User 123
GET /orders
200 OK
50 ms
```

---

# Tracing

Example:

```text
Gateway
   ↓
Order Service
   ↓
Payment Service
   ↓
Inventory Service
```

Gateway creates:

```text
Trace ID
```

Example:

```text
trace-id = abc123
```

Passed through all services.

---

# IP Whitelisting / Blacklisting

Allow:

```text
192.168.x.x
```

Block:

```text
Known malicious IPs
```

---

# Versioning

Example:

```text
/v1/products
/v2/products
```

Gateway routes:

```text
v1 → old service
v2 → new service
```

---

# API Gateway Advantages

## Encapsulation

Clients know:

```text
api.company.com
```

They do not know internal services.

---

## Centralized View

Operations teams get:

- Monitoring
- Metrics
- Security controls

from one place.

---

## Simpler Client Code

Instead of:

```text
5 services
5 endpoints
5 auth flows
```

Client calls:

```text
1 gateway
```

---

## Better Monitoring

Gateway sees all traffic.

Useful for:

- Analytics
- Metrics
- Logging
- Tracing

---

# API Gateway Disadvantages

## Single Point of Failure

Gateway failure:

```text
Client
   ↓
Gateway ❌
```

Entire system inaccessible.

---

### Misconception

API Gateway introduces a SPOF.

### Correction

Exactly like load balancers, deploy multiple gateway instances.

Example:

```text
Clients
    ↓
Load Balancer
    ↓
Gateway A
Gateway B
Gateway C
```

---

## Performance Impact

Extra network hop:

```text
Client
   ↓
Gateway
   ↓
Service
```

Adds latency.

---

## Bottleneck

Example:

```text
100 services
       ↑
1 overloaded gateway
```

---

## Configuration Complexity

Must manage:

- Routes
- Security
- Certificates
- Monitoring
- Service discovery
- Rate limits

---

# Backend For Frontend (BFF)

---

# Problem BFF Solves

Different clients need different responses.

Example:

```text
Mobile App
Web App
Smart TV App
```

---

## Mobile Response

```json
{
  "name": "iPhone"
}
```

---

## Web Response

```json
{
  "name": "iPhone",
  "reviews": [...],
  "recommendations": [...],
  "inventory": {...}
}
```

---

# Without BFF

One backend serves everyone.

Eventually:

```text
if mobile...
if web...
if tablet...
if tv...
```

Backend becomes messy.

---

# BFF Architecture

```text
Mobile App
      ↓
Mobile BFF
      ↓
Services

Web App
      ↓
Web BFF
      ↓
Services
```

---

# Responsibilities

BFF:

1. Fetch data
2. Format data
3. Filter fields
4. Return client-specific response

---

# Data Transformation Example

Services return:

```json
{
  "name": "iPhone",
  "description": "...",
  "reviews": [...],
  "inventory": {...},
  "supplierInfo": {...}
}
```

Mobile only needs:

```json
{
  "name": "iPhone"
}
```

BFF transforms response.

---

# When To Use BFF

Use when:

- Shared backend becoming difficult to maintain
- Different clients require different responses
- Too many frontend-specific conditions exist

Example:

```text
if mobile
if web
if tablet
if tv
```

---

# GraphQL and BFF

Observation:

GraphQL works very well as a BFF.

Example:

```graphql
{
  product {
    name
    price
  }
}
```

Different clients request different fields.

---

# API Gateway vs BFF

## API Gateway

Focus:

```text
Infrastructure concerns
```

Examples:

- Authentication
- Routing
- Rate limiting
- Logging
- Load balancing

---

## BFF

Focus:

```text
Frontend-specific responses
```

Examples:

- Data aggregation
- Data formatting
- Client-specific APIs

---

# Combined Architecture

```text
Client
   ↓
API Gateway
   ↓
BFF
   ↓
Microservices
```

Gateway handles platform concerns.

BFF handles frontend concerns.

---

# API Gateway Technologies Mentioned

- Amazon API Gateway
- Apigee
- Azure API Gateway
- Kong

---

# High-Level API Gateway Mental Model

Think:

```text
Reverse Proxy
+
Authentication
+
Rate Limiting
+
Routing
+
Load Balancing
+
Caching
+
Monitoring
+
Service Discovery
```

---

# High-Level BFF Mental Model

Think:

```text
Frontend-specific API Composer
+
Response Formatter
```

---

# Circuit Breaker

---

# User Question

The user asked:

- What exactly is a circuit breaker?
- Why was it created?
- Netflix made it, right?
- When should we use it?

---

# Problem Circuit Breakers Solve

Architecture:

```text
User Service
    ↓
Order Service
    ↓
Payment Service
```

Payment becomes slow.

Instead of:

```text
50ms
```

responses become:

```text
10 seconds
```

Order requests accumulate.

---

# Retry Storm

Developers often retry.

Example:

```text
Call Payment
    ↓
Timeout
    ↓
Retry
    ↓
Timeout
    ↓
Retry
```

One request becomes:

```text
3 requests
```

---

Example:

```text
1000 RPS
```

With retries:

```text
3000 calls/sec
```

Service receives even more load.

This was called:

```text
Retry Storm
```

---

# Cascading Failure

Without breaker:

```text
Client
   ↓
Order Service
   ↓
Payment Service (slow)
```

Order threads fill.

Then:

```text
Order Service slows
```

Then:

```text
Cart Service
Shipping Service
Inventory Service
```

also become slow.

Entire system appears down.

---

# Electrical Analogy

House circuit breaker:

```text
Too much current
    ↓
Breaker trips
    ↓
Power cut
```

Purpose:

Protect system.

Software breaker does same.

---

# Basic Behavior

Suppose:

```text
Order Service
    ↓
Payment Service
```

After enough failures:

```text
50 failures
```

Breaker opens.

---

# Open State

Instead of:

```text
Order Service
    ↓
Payment
```

becomes:

```text
Order Service
    ↓
Immediate Failure
```

No network call made.

Example response:

```text
Payment unavailable
```

---

# Why Immediate Failure Is Better

Without breaker:

```text
Wait 10 seconds
Timeout
```

User waits and still fails.

---

With breaker:

```text
Fail immediately
```

Response may arrive in milliseconds.

System remains healthy.

---

# Circuit Breaker States

## Closed

Normal.

```text
Order Service
    ↓
Payment Service
```

Traffic allowed.

---

## Open

Too many failures.

```text
Order Service
    X
Payment Service
```

Requests blocked.

---

## Half-Open

After some time:

```text
30 seconds
```

Try a few requests.

Example:

```text
5 test requests
```

If successful:

```text
Close breaker
```

If failed:

```text
Open again
```

---

# State Diagram

```text
        Failures
Closed ----------> Open
   ^                 |
   |                 |
   |                 |
   |    Success      |
   +---- Half Open <-+
```

---

# Example Configuration

```text
Failure Threshold = 50%

Minimum Requests = 100

Open Duration = 30 sec
```

If:

```text
100 requests observed
50+ fail
```

Open breaker.

---

# Netflix Context

Netflix had many services:

```text
Movie Service
Recommendation Service
User Service
Billing Service
Viewing Service
```

Failures caused:

- Thread exhaustion
- Retry storms
- Cascading failures

Netflix popularized the pattern via:

```text
Hystrix
```

---

# Hystrix Features

## Circuit Breaking

Stop calling unhealthy services.

---

## Fallbacks

Instead of:

```text
Error
```

Return:

```text
Top Trending Movies
```

when recommendation service is unavailable.

---

## Isolation

Separate thread pools:

```text
Recommendations → Pool A
Reviews → Pool B
Payments → Pool C
```

---

# Netflix Philosophy

Question:

If Recommendations fail, should homepage fail?

Answer:

```text
No
```

Show homepage without recommendations.

---

# Retry vs Circuit Breaker

### Misconception

Retries and circuit breakers solve the same problem.

### Correction

Retry:

```text
Maybe temporary.
Try again.
```

Circuit Breaker:

```text
Service unhealthy.
Stop sending traffic.
```

Often used together.

---

# Timeout + Circuit Breaker

Typical configuration:

```text
Call Payment
```

If response exceeds:

```text
2 seconds
```

Count as failure.

Too many failures:

```text
Open breaker
```

---

# Circuit Breaker With Load Balancing

Example:

```text
Payment-A
Payment-B
Payment-C
```

If:

```text
Payment-B unhealthy
```

Circuit breaker can stop sending traffic there.

Continue using:

```text
Payment-A
Payment-C
```

---

# Real-World Examples

## Stripe

If Stripe is down:

```text
Stop calling Stripe
Return payment unavailable
```

---

## Recommendation Service

If recommendations fail:

```text
Show popular products
```

---

## Weather Service

If external weather API fails:

```text
Return cached weather
```

---

# When To Use

Use when calling:

```text
Microservice
Database
Redis
External APIs
```

Anything over network boundaries.

---

# When Not Needed

Usually unnecessary around:

```java
calculateTax()
```

Pure in-memory local code.

---

# Follow-Up: How Circuit Breaker Is Actually Implemented

User confusion:

- How does it actually trip?
- How are errors tracked?
- How is it implemented in code?
- How does fallback get triggered?
- Which errors count?
- How does state get stored?

---

# Where Circuit Breaker Lives

Architecture:

```text
Order Service
    ↓
Circuit Breaker
    ↓
Payment Service
```

Circuit breaker exists in caller.

Not callee.

---

### Misconception

Circuit breaker exists inside Payment Service.

### Correction

Circuit breaker usually exists in the service making the call.

The caller protects itself.

---

# What It Really Is

At core:

```java
class PaymentCircuitBreaker {

    State state;

    int successCount;
    int failureCount;

    long lastFailureTime;
}
```

Just a state machine.

No magic.

---

# Without Breaker

```java
public PaymentResponse pay(...) {
    return paymentClient.call(...);
}
```

---

# With Breaker

```java
public PaymentResponse pay(...) {

    if(circuitBreaker.isOpen()) {
        return fallbackResponse();
    }

    try {

        PaymentResponse response =
            paymentClient.call(...);

        circuitBreaker.recordSuccess();

        return response;

    } catch(Exception ex) {

        circuitBreaker.recordFailure();

        throw ex;
    }
}
```

Every request updates breaker state.

---

# Request Flow Example

Initially:

```text
State = CLOSED
```

---

Request #1:

Success.

```text
success++
```

State remains closed.

---

Request #2:

Timeout.

```text
failure++
```

Still closed.

---

Request #100:

```text
100 requests observed
60 failures
```

Failure rate:

```text
60%
```

Threshold:

```text
50%
```

Open breaker.

---

# What Happens After Open

Before calling service:

```java
if(circuitBreaker.isOpen()) {
   return fallback();
}
```

Network call skipped entirely.

---

# How Failure Rate Is Calculated

Example outcomes:

```text
Success
Success
Failure
Failure
Success
Failure
```

Implementation may store:

```java
List<Boolean> last100Requests;
```

or ring buffer.

Then:

```java
failureRate =
  failures / totalRequests
```

Example:

```text
100 requests
60 failures
```

Failure rate:

```text
60%
```

Breaker opens.

---

# Which Errors Count

Important point.

### Misconception

Any error should trip breaker.

### Correction

Only dependency-health-related failures usually count.

---

404 example:

```text
GET /user/99999
```

returns:

```text
404
```

Service healthy.

Should not open breaker.

---

Usually count:

```text
Connection refused
Connection timeout
Socket timeout
503
5xx errors
```

---

Possible configuration:

```java
recordException(
    ex instanceof TimeoutException
);
```

or

```java
recordResult(
    response.status >= 500
);
```

---

# Half-Open Implementation

Suppose:

```text
Circuit = OPEN
```

Opened:

```text
10:00
```

Configured:

```text
Wait 30 sec
```

At:

```text
10:00:30
```

State becomes:

```text
HALF_OPEN
```

Allow:

```text
5 requests
```

---

If all succeed:

```text
Close breaker
```

---

If failures continue:

```text
Open breaker again
```

---

# Where State Is Stored

Typically inside application instance memory.

Example:

```text
Order Service Instance 1
    Own Breaker

Order Service Instance 2
    Own Breaker

Order Service Instance 3
    Own Breaker
```

---

### Misconception

There is one central circuit breaker server.

### Correction

Each service instance usually maintains its own breaker state.

Example:

```text
Pod 1 → own breaker
Pod 2 → own breaker
Pod 3 → own breaker
```

---

# Spring Boot Example

Modern libraries:

- Resilience4j
- Spring Cloud Circuit Breaker

Older:

- Netflix Hystrix

---

Example:

```java
@CircuitBreaker(
    name = "paymentService",
    fallbackMethod = "fallback"
)
public PaymentResponse makePayment() {

    return paymentClient.call();
}
```

Framework automatically:

- Tracks requests
- Tracks failures
- Calculates failure %
- Opens breaker
- Closes breaker
- Handles half-open state

---

Fallback:

```java
public PaymentResponse fallback(Exception ex) {

    return new PaymentResponse(
        "Payment temporarily unavailable"
    );
}
```

---

# Recommendation Example

Architecture:

```text
Product Page
      ↓
Recommendation Service
```

Without breaker:

```text
Page request
   ↓
Wait 10 sec
   ↓
Timeout
```

---

With breaker:

```text
Page request
   ↓
Breaker open
   ↓
Return trending products
```

---

# What Is Actually Being Tripped?

### Misconception

A signal is sent to Payment Service saying:

```text
You are blocked.
```

### Correction

Nothing happens to Payment Service.

Caller simply decides:

```text
I won't call you anymore.
```

Analogy:

```text
Friend not answering phone
```

Eventually:

```text
I stop calling.
```

Friend's phone remains unchanged.

---

# Circuit Breaker + Load Balancer

Example:

```text
Payment-A
Payment-B
Payment-C
```

Possible state:

```text
Breaker A = Closed
Breaker B = Open
Breaker C = Closed
```

Requests go only to:

```text
Payment-A
Payment-C
```

---

# Final SDE2 Mental Model

Circuit Breaker is:

```text
State Machine
+
Failure Statistics
+
Decision:
"Should I even attempt this call?"
```

Every request:

```text
Check breaker state
      ↓
Allowed?
      ↓
Call dependency
      ↓
Record result
      ↓
Update breaker state
```

Frameworks like Resilience4j and Hystrix automate the bookkeeping, but conceptually that's all a circuit breaker is.