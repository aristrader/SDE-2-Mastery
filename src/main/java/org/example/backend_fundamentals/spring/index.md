---
order: 20
---


# Spring

Spring & Spring Boot topics — framework theory for interview prep.

This section covers the core architecture, data access abstractions, web request pipeline, security framework, and cloud integrations that power modern enterprise Java applications.

## Prerequisites

- **Java OOP & Language Foundations:** Interfaces, abstract classes, polymorphism, and Java memory model fundamentals.
- **Reflection & Dynamic Proxies:** How runtime proxies intercept invocations (JDK dynamic proxies vs CGLIB).
- **Concurrency & Threads:** Understanding thread execution, thread pools, and ThreadLocal context propagation.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `spring_core` | Inversion of Control, dependency injection, bean lifecycle, declarative transactions, caching, and async execution. |
| 2 | `spring_web` | DispatcherServlet pipeline, REST controllers, Jackson data binding, global exception handling, and WebFlux threading. |
| 3 | `spring_data` | JPA persistence context, entity lifecycle states, repository hierarchy, derived queries, and N+1 query avoidance. |
| 4 | `spring_security` | Filter chain architecture, authentication providers, method security, and JWT/OAuth2 resource server validation. |
| 5 | `spring_cloud` | Microservice inter-service communication and declarative HTTP client mechanics with OpenFeign. |

## Next action

Start with `spring_core` to master container mechanics, bean lifecycle hooks, and proxy-based transaction management. If you are preparing specifically for runtime bootstrapping, dependency starters, or production operations, pair this with the sibling `spring_boot` module.

## Quick recall

**Q. What is the fundamental problem Spring's IoC container solves?**
A. It decouples component creation and lifecycle management from business logic, allowing dependencies to be wired declaratively rather than hardcoded with `new`.

**Q. How does Spring implement declarative behavior like `@Transactional` and `@Cacheable`?**
A. Via runtime proxies (CGLIB subclassing or JDK dynamic proxies) that intercept method invocations to manage transactions or cache lookups around the target invocation.

**Q. Why does calling an annotated method from within the same class bypass the annotation?**
A. Self-invocation calls `this.targetMethod()` directly on the unproxied instance, bypassing the intercepting proxy.
