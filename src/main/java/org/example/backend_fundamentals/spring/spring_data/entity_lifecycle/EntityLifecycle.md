# JPA Entity Lifecycle

---

## The four entity states

| State | DB row exists? | Tracked by PC? | Changes auto-flushed? |
|---|---|---|---|
| **Transient** | No | No | No |
| **Managed** | Yes (or pending INSERT) | Yes | Yes |
| **Detached** | Yes | No | No |
| **Removed** | Yes (pending DELETE) | Yes (until flush) | N/A — DELETE on flush |

**Persistence context (PC)** is the first-level cache — a unit-of-work scope, typically one transaction. Hibernate tracks managed entities here.

---

## Transient

A plain Java object with no persistence context association:

```java
Employee e = new Employee();  // transient — no DB row, not tracked
e.setName("Alice");
```

No `@Id` value (or null if auto-generated), no snapshot in Hibernate's identity map.

---

## Managed

The entity is associated with an active persistence context. Hibernate holds a **snapshot** of the state at load time; at flush time it compares current state to the snapshot and issues an `UPDATE` only for changed fields.

```java
// Load → managed
Employee e = em.find(Employee.class, 1L);
e.setSalary(120_000);           // change tracked automatically
// at transaction commit → Hibernate generates UPDATE WHERE id=1
// No explicit save() needed
```

**Identity guarantee:** loading the same entity twice in the same session returns the **same Java object** — not a copy.

```java
Employee a = em.find(Employee.class, 1L);
Employee b = em.find(Employee.class, 1L);
assert a == b;  // true — same reference
```

---

## Detached

The entity was managed but the persistence context was closed (transaction ended, session closed, or explicit `detach()`). Changes to a detached entity are **invisible** to Hibernate.

```java
Employee e = em.find(Employee.class, 1L);
em.detach(e);         // or em.clear() to detach all
e.setSalary(999_999); // change NOT tracked — no UPDATE will happen
```

Common causes of becoming detached:
- Transaction boundary closes → session closes → all entities detach
- `em.detach(entity)` / `em.clear()`
- Entity returned from a `@Transactional` method to a non-transactional caller

---

## Removed

Scheduled for deletion. Hibernate will issue a `DELETE` on next flush.

```java
Employee e = em.find(Employee.class, 1L);
em.remove(e);  // state: removed
// at flush/commit → DELETE FROM employee WHERE id=1
```

After removal, the entity transitions back to transient (no longer tracked).

---

## State transition map

```
new object
    │
    │ persist() / repository.save() [isNew = true]
    ▼
 Managed ◄──────────── merge() ────────── Detached
    │                                        ▲
    │ transaction ends / detach() / clear()  │
    └────────────────────────────────────────┘
    │
    │ remove() / repository.delete()
    ▼
 Removed
    │ flush/commit
    ▼
 (transient again)
```

---

## Second-level cache

The **first-level cache** (persistence context) lives for one session/transaction — the identity map described above.

The **second-level cache** (L2C) is process-wide and survives across sessions. Hibernate supports it via pluggable providers (Ehcache, Redis, Infinispan).

```java
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)  // L2C enabled per entity
public class Employee { ... }
```

| Cache | Scope | Lifespan | Enabled by default? |
|---|---|---|---|
| First-level (persistence context) | Single session/transaction | Transaction duration | Always on |
| Second-level | Application-wide | Configurable TTL | Off — must enable |
| Query cache | Application-wide | Configurable TTL | Off — must enable |

**L2C pitfall:** bypassing Hibernate (native SQL, JDBC) doesn't invalidate the L2C — stale reads possible. L2C also adds complexity in clustered deployments; only cache read-heavy, rarely-modified entities.

---

## Lifecycle callbacks

JPA fires callbacks at specific state transitions. Annotate a method on the entity (or a separate `EntityListener` class):

| Annotation | Fires |
|---|---|
| `@PrePersist` | Before `INSERT` (before persist() or save() flushes) |
| `@PostPersist` | After `INSERT` succeeds |
| `@PreUpdate` | Before `UPDATE` is issued at flush |
| `@PostUpdate` | After `UPDATE` succeeds |
| `@PreRemove` | Before `DELETE` is issued |
| `@PostRemove` | After `DELETE` succeeds |
| `@PostLoad` | After the entity is loaded / refreshed into the persistence context |

```java
@Entity
public class Employee {

    @PrePersist
    void onPrePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    @PostLoad
    void onPostLoad() {
        // e.g., decrypt a sensitive field after load
    }
}
```

**Interview note:** `@PostLoad` fires for both `em.find()` and JPQL query results. It does NOT fire on in-memory construction (transient state).

---

## Identity generation strategies

| Strategy | How the ID is generated | Portable? | Notes |
|---|---|---|---|
| `IDENTITY` | DB auto-increment column | DB-dependent | Hibernate can't batch `INSERT`s — each insert needs a round-trip to get the generated ID |
| `SEQUENCE` | DB sequence object | Yes (PostgreSQL, Oracle) | Hibernate pre-allocates a block of IDs (`allocationSize`, default 50) — batching-friendly |
| `TABLE` | A dedicated DB table simulating a sequence | Yes | Portable but slow (row-level lock); avoid in high-throughput systems |
| `AUTO` | Hibernate picks based on dialect | Yes | Usually maps to `SEQUENCE` on modern DBs; `IDENTITY` on MySQL |

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE,
                generator = "emp_seq")
@SequenceGenerator(name = "emp_seq", sequenceName = "employee_seq",
                   allocationSize = 50)
private Long id;
```

**`IDENTITY` vs `SEQUENCE` trade-off:** `IDENTITY` is the default on MySQL/MariaDB but breaks Hibernate's JDBC batch insert optimization because each insert requires an immediate round-trip for the generated key. `SEQUENCE` with `allocationSize > 1` lets Hibernate allocate a block locally and batch multiple inserts without extra round-trips.

---

## Dirty checking internals

Hibernate uses a **hydrated state** (original column values stored as an `Object[]` in the `EntityEntry`) as the snapshot. At flush time it walks every managed entity and compares field-by-field. If anything changed, it generates an `UPDATE` for just those columns (`@DynamicUpdate` generates the minimal SQL; default includes all columns for simplicity).

Dirty checking only works while the entity is **managed**. Detached entity changes are silent.

---

## persist() vs merge() vs save()

| Method | Works on | What it does |
|---|---|---|
| `em.persist()` | Transient only | Makes transient → managed; throws `EntityExistsException` if detached |
| `em.merge()` | Transient or detached | Copies state onto a managed instance; returns the **new managed** copy; the argument remains detached |
| `repository.save()` | Both | Calls `persist()` if `isNew()` (null `@Id`); calls `merge()` if entity already has an id |

**merge() trap:** the object you pass to `merge()` is **not** the managed instance. If you keep using the original reference, changes still aren't tracked.

```java
Employee detached = ...;
Employee managed = em.merge(detached);  // managed is now tracked
detached.setSalary(50_000);             // detached — still not tracked
managed.setSalary(50_000);              // managed — tracked, will flush
```

---

## LazyInitializationException

Accessing a lazy association **outside the transaction** (after the session closes) throws `LazyInitializationException` — the entity is detached and the proxy can't initialize.

```java
// @OneToMany(fetch = FetchType.LAZY) — default
Employee e = employeeRepository.findById(1L).get();  // transaction ends here in naive setup
e.getProjects().size();  // BOOM — session already closed
```

**Fixes:**

| Approach | When to use |
|---|---|
| `@Transactional` on the service method | Keep session open through the full business method |
| `JOIN FETCH` / `EntityGraph` | Eagerly load what you know you need |
| DTO projection (`interface` or `@Query` with constructor) | Avoid entity graph entirely — best for read-only |
| `spring.jpa.open-in-view=true` (anti-pattern) | Keeps session open through the view layer; hides the problem, causes N+1 |

---

## Quick recall

**Q. What is the persistence context?**
A. A first-level cache and unit-of-work that tracks managed entities for one session/transaction; same entity loaded twice returns the same Java object.

**Q. Why don't you need to call save() after changing a managed entity?**
A. Hibernate snapshots entity state at load; dirty checking at flush time generates UPDATE automatically for any changed fields.

**Q. What happens when you call merge() on a detached entity?**
A. Hibernate copies the detached state onto a new managed instance and returns it; the original argument stays detached — use the return value.

**Q. What is the dirty checking snapshot stored as?**
A. A hydrated Object[] array (the original column values) stored in Hibernate's EntityEntry; compared field-by-field at flush time.

**Q. First-level cache vs second-level cache — key difference?**
A. First-level is per-session (always on, identity map); second-level is process-wide, opt-in, and requires a cache provider (Ehcache, Redis).

**Q. Why does IDENTITY generation strategy break JDBC batch inserts?**
A. Each INSERT needs an immediate round-trip to retrieve the DB-generated key; Hibernate can't accumulate a batch. SEQUENCE with allocationSize > 1 avoids this.

**Q. When does @PostLoad fire vs @PrePersist?**
A. @PostLoad fires after an entity is loaded/refreshed from DB; @PrePersist fires before the first INSERT (not on load).
