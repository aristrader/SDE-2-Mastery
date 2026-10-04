---
order: 10
---

# Spring Core

Overview of Spring Core.

Spring Core provides the foundational inversion-of-control container, dependency injection mechanics, bean lifecycle governance, and AOP-driven cross-cutting infrastructure (transactions, caching, and async task execution) upon which the rest of the Spring framework is built.

## Prerequisites

- Solid grasp of Java interfaces, inheritance, and runtime polymorphism.
- Familiarity with Java reflection and dynamic proxy concepts.
- Understanding of database transactions (ACID, isolation levels) and thread execution models.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `dependency_injection` | Constructor, setter, and field injection tradeoffs, immutability, and circular dependency resolution. |
| 2 | `ioc_container` | BeanFactory vs ApplicationContext, bean definitions, scopes (singleton, prototype, web), and container startup. |
| 3 | `bean_lifecycle` | Initialization sequence, Aware interfaces, BeanPostProcessor hooks, `@PostConstruct`, and `@PreDestroy`. |
| 4 | `caching` | Cache abstraction (`@Cacheable`, `@CachePut`, `@CacheEvict`), provider adapters, and proxy self-invocation. |
| 5 | `transactions` | Declarative `@Transactional` mechanics, propagation modes, isolation levels, rollback triggers, and proxy traps. |
| 6 | `async_mdc` | `@Async` execution, ThreadPoolTaskExecutor configuration, and ThreadLocal context propagation via TaskDecorator. |

## Next action

Begin with `dependency_injection` and `ioc_container` to solidify bean creation fundamentals before diving into proxy-based cross-cutting concerns in `transactions` and `caching`.

## Quick recall

**Q. What is the difference between `BeanFactory` and `ApplicationContext`?**
A. `BeanFactory` provides basic lazy bean instantiation and wiring. `ApplicationContext` extends it with eager singleton pre-instantiation, AOP integration, event publishing, and environment abstraction.

**Q. Why is constructor injection preferred over field injection?**
A. It enforces immutability (`final` fields), guarantees non-null dependencies at construction time, prevents partial initialization, and enables easy unit testing without reflection.

**Q. What is the role of a `BeanPostProcessor`?**
A. It intercepts bean instances before and after custom initialization, enabling framework extensions such as creating AOP proxies, processing custom annotations, or wrapping beans.

**Q. What happens during `@Transactional` self-invocation?**
A. The inner call bypasses the Spring proxy, running as a standard internal method call without transaction interception or propagation.
