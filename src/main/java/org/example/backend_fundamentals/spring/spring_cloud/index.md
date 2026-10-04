---
order: 70
---

# Spring Cloud

Spring Cloud provides tools and abstractions for building resilient, distributed microservice architectures. It simplifies inter-service communication, dynamic service discovery, distributed configuration, and fault-tolerant network communication.

## Prerequisites

- Solid understanding of Spring Web and REST controller communication.
- Microservice architecture concepts: distributed calls, network latency, timeouts, and partial failure.
- HTTP client patterns and basic serialization with Jackson.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `feign` | Declarative HTTP client interfaces, request interceptors (auth token propagation), error decoders, and timeout/retry tuning with OpenFeign. |

## Next action

Proceed to `feign` to examine declarative REST client interface generation, interceptor-based header propagation, and custom error decoding across microservice boundaries.

## Quick recall

**Q. What is Spring Cloud OpenFeign?**
A. A declarative HTTP client library that generates runtime proxy implementations for Java interfaces annotated with Spring MVC or Feign annotations, abstracting away manual HTTP calls.

**Q. How do you propagate security headers (like Bearer tokens) across Feign calls?**
A. Implement a `RequestInterceptor` bean that extracts the authorization header from the current incoming `RequestContextHolder` / `SecurityContextHolder` and appends it to the outgoing Feign `RequestTemplate`.

**Q. How does Feign handle downstream HTTP error responses?**
A. By default, Feign throws a `FeignException` for non-2xx statuses; you can configure a custom `ErrorDecoder` to translate specific HTTP status codes into domain-specific exceptions.
