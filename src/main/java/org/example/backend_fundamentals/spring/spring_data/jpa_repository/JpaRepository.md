# JPA Repository Hierarchy

---

## The hierarchy at a glance

```
Repository  (marker — no methods)
  └── CrudRepository  (save, findById, findAll, delete, count, exists)
        └── PagingAndSortingRepository  (+ findAll(Pageable), findAll(Sort))
              └── JpaRepository  (+ flush, saveAndFlush, deleteAllInBatch, getReferenceById)
```

`Repository` is a pure marker — Spring Data uses it to detect what to proxy. `CrudRepository` gives the 13 basic CRUD methods. `PagingAndSortingRepository` layers sorting and pagination. `JpaRepository` adds JPA-specific operations with no meaning outside a JPA context.

---

## What JpaRepository adds over CrudRepository

| Method | What it does |
|---|---|
| `flush()` | Synchronise persistence context to DB immediately |
| `saveAndFlush(entity)` | `save` + immediate `flush` in one call |
| `deleteAllInBatch(entities)` | Single DELETE … WHERE id IN (…) — avoids N individual DELETEs |
| `deleteAllInBatch()` | Truncate-like DELETE with no WHERE — use with care |
| `getReferenceById(id)` | Returns a Hibernate proxy; no SELECT issued until a field is accessed |
| `findAll(Example)` | Query-by-example support (inherited via `QueryByExampleExecutor`) |

Batch deletes are the most practically useful — replacing a loop of `deleteById` calls with one SQL statement is a common performance fix.

---

## @EnableJpaRepositories — scanning and configuration

Spring Boot auto-configures `@EnableJpaRepositories` via `JpaRepositoriesAutoConfiguration`. You need it explicitly only when:

- You have multiple `DataSource` / `EntityManagerFactory` beans and need to bind repositories to a specific one.
- Your repository interfaces live outside the `@SpringBootApplication` package (and you're not using component scan).

```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.repositories",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryJpaConfig { ... }
```

Without `basePackages`, Spring scans from the `@EnableJpaRepositories`-annotated class's package downward. Misconfiguring this in multi-datasource setups is a common source of "no qualifying bean of type Repository" errors at startup.

---

## How Spring Data generates implementations at runtime

Spring Data doesn't generate bytecode at compile time. At startup:

1. `JpaRepositoriesAutoConfiguration` triggers a `JpaRepositoryFactory`.
2. The factory inspects every interface that extends `Repository` (or a sub-interface).
3. For each interface, it creates a **JDK dynamic proxy** backed by `SimpleJpaRepository<T, ID>`.
4. Method calls are dispatched: if the method is declared in `SimpleJpaRepository` (e.g., `save`, `findById`), that implementation runs directly. If the method name follows a naming convention (derived query), a `PartTreeJpaQuery` is built and cached at startup. If `@Query` is present, a `NamedQuery` or `NativeQuery` is built instead.

`SimpleJpaRepository` is the implementation worth knowing — it wraps `EntityManager` and contains the concrete code behind every standard repository method.

---

## @Transactional on SimpleJpaRepository

```java
// Simplified view of SimpleJpaRepository
@Repository
@Transactional(readOnly = true)           // class-level default
public class SimpleJpaRepository<T, ID> {

    @Transactional                         // overrides class-level for writes
    public <S extends T> S save(S entity) { ... }

    @Transactional                         // overrides class-level for writes
    public void delete(T entity) { ... }

    // findById, findAll — inherit class-level readOnly = true
}
```

Key consequences:

- `save()` and `delete()` are `@Transactional` — Spring opens a transaction if none exists, commits on return. Calling `save()` from outside a `@Transactional` service works but creates a short-lived per-save transaction. Prefer service-level transactions so the entire unit of work commits atomically.
- `findById()` and `findAll()` are `readOnly = true` — Hibernate skips dirty checking on returned entities, reducing overhead.
- Multiple repository calls in a service with no outer `@Transactional` each get their own transaction — breaks atomicity and blocks first-level cache sharing.

**Rule of thumb:** always put `@Transactional` at the service layer, not just on individual repository calls. The repository's built-in `@Transactional` is a safety net, not the right boundary.

---

## saveAndFlush vs save

| | `save()` | `saveAndFlush()` |
|---|---|---|
| When SQL is sent | At transaction commit (or explicit flush) | Immediately |
| Use case | Normal persistence — let the ORM batch | Need DB to see data before TX ends |
| Risk | Stale reads within the same TX | Extra round-trip; breaks write batching |

**When saveAndFlush matters:** calling a stored procedure or native query in the same transaction that must read the just-persisted rows — the DB sees the data only after it's flushed to the connection. Also: integration tests asserting DB state mid-transaction (though test isolation usually avoids this).

---

## save() — persist vs merge

`save()` in `SimpleJpaRepository` does different things depending on whether the entity is new:

```java
// SimpleJpaRepository.save() — simplified
public <S extends T> S save(S entity) {
    if (entityInformation.isNew(entity)) {
        em.persist(entity);     // INSERT — entity becomes managed
        return entity;
    } else {
        return em.merge(entity); // SELECT + UPDATE (or just UPDATE if already in PC)
    }
}
```

- **New entity** (`id == null`, or implements `Persistable` and `isNew()` returns true): calls `EntityManager.persist()` — no SELECT, entity is attached to the persistence context.
- **Existing entity** (has an id): calls `EntityManager.merge()` — Hibernate may issue a SELECT to load the managed version, copies state from the detached object, then marks it dirty for UPDATE at flush. The returned reference is the managed entity; **the passed-in object remains detached**.

**Interview trap:** calling `save(existingEntity)` and continuing to use the passed-in reference instead of the returned one. Changes to the passed-in object after `save()` are not tracked.

**`isNew` determination:** Spring Data checks if the `@Id` field is `null` (or `0` for primitives). `@GeneratedValue` works naturally. For manually assigned IDs (e.g., UUIDs), implement `Persistable<ID>` to override `isNew()` — otherwise `save()` always calls `merge()`, issuing a needless SELECT.

---

## getReferenceById vs findById

| | `getReferenceById(id)` | `findById(id)` |
|---|---|---|
| SQL issued | None (lazy proxy) | `SELECT * FROM … WHERE id = ?` immediately |
| Returns | Hibernate proxy object | `Optional<T>` (present if found) |
| LazyInitializationException | Thrown if proxy accessed outside TX | Never — entity is fully loaded |
| Primary use case | Setting a FK association without loading the entity | When you actually need the entity's fields |

```java
// Efficient FK assignment — no SELECT for the Author
Book book = new Book();
book.setAuthor(authorRepository.getReferenceById(authorId));  // proxy only
bookRepository.save(book);

// Versus — issues a SELECT just to get the ID back for FK
Author author = authorRepository.findById(authorId).orElseThrow();
book.setAuthor(author);
```

Don't confuse with the deprecated `getById` / `getOne` — prefer `getReferenceById` (Spring Data 2.7+).

---

## Quick recall

**Q. What does JpaRepository add over CrudRepository?**
A. JPA-specific ops: `flush`, `saveAndFlush`, batch deletes (`deleteAllInBatch`), and `getReferenceById` (proxy, no SELECT).

**Q. What backs every Spring Data repository at runtime?**
A. A JDK proxy dispatching to `SimpleJpaRepository`, built by `JpaRepositoryFactory` at startup.

**Q. Why is `SimpleJpaRepository` marked `@Transactional(readOnly = true)` at class level?**
A. All read methods inherit it — Hibernate skips dirty checking, reducing overhead. Write methods override with `@Transactional`.

**Q. When would you use `saveAndFlush` instead of `save`?**
A. When you need the DB to see the data before the transaction ends — e.g., before calling a stored procedure in the same TX.

**Q. `getReferenceById` vs `findById` — what's the practical difference?**
A. `getReferenceById` returns a proxy with no SELECT; `findById` hits the DB immediately and returns `Optional<T>`. Use the proxy for FK-only assignments.

**Q. What goes wrong if you call `repository.save()` in a loop with no outer `@Transactional`?**
A. Each save gets its own transaction — no atomicity, no batching, higher overhead. Wrap in a service-level `@Transactional`.

**Q. `save()` on a new entity vs an existing entity — what Hibernate operation runs?**
A. New entity (null id): `EntityManager.persist()` — direct INSERT, no SELECT. Existing entity (non-null id): `EntityManager.merge()` — may SELECT first, then UPDATE. Always use the returned reference after `merge()`.

**Q. When do you need `@EnableJpaRepositories` explicitly in Spring Boot?**
A. In multi-datasource setups to bind specific repository packages to a specific `EntityManagerFactory`/`TransactionManager`. Boot's auto-configuration handles single-datasource projects automatically.
