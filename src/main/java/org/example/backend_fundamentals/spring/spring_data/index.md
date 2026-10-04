---
order: 40
---

# Spring Data

Spring Data provides a unified data-access abstraction over relational and NoSQL datastores. In the JPA module, it bridges Spring applications with Hibernate and the JPA specification, handling repository generation, query derivation, and persistence context lifecycle management.

## Prerequisites

- Relational database fundamentals: primary/foreign keys, joins, indexes, ACID transactions, and isolation levels.
- Object-relational mapping (ORM) concepts and JPA `EntityManager` fundamentals.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `entity_lifecycle` | JPA persistence states (transient, managed, detached, removed), flush vs commit, and first-level caching. |
| 2 | `jpa_repository` | Repository hierarchy (`CrudRepository`, `ListCrudRepository`, `JpaRepository`) and dynamic proxy implementation. |
| 3 | `derived_queries` | Query generation from method signatures, JPQL with `@Query`, native SQL queries, and `@Modifying` updates. |
| 4 | `jpa_lazy_eager` | Lazy vs eager fetch strategies, bytecode proxies, N+1 query problem diagnosis, and resolution via JOIN FETCH and `@EntityGraph`. |

## Next action

Start with `entity_lifecycle` to understand the JPA persistence context and dirty-checking mechanism before defining repository interfaces or tuning query execution.

## Quick recall

**Q. What are the four JPA entity lifecycle states?**
A. Transient (new, unpersisted), Managed (associated with an active persistence context), Detached (previously managed, session closed), and Removed (marked for deletion on next flush).

**Q. How does Spring Data generate repository implementations without manual code?**
A. It creates dynamic interface proxies at startup backed by `SimpleJpaRepository`, translating method calls into JPA queries or JPQL executions.

**Q. What causes the JPA N+1 select problem?**
A. Querying N parent entities with lazy-loaded child relationships results in 1 initial query for the parents plus N separate queries for each child collection upon traversal.

**Q. How do you resolve the N+1 select problem in Spring Data JPA?**
A. Use `JOIN FETCH` in a custom JPQL `@Query`, apply an `@EntityGraph` annotation on the repository method, or use DTO projections with explicit joins.
