---
order: 10
---

# JPA Entity Lifecycle

JPA entity lifecycle explains when Hibernate tracks an object and when it does not. For interviews, focus on transient, managed, detached, removed, dirty checking, `persist` vs `merge`, and lazy loading failures.

---

## Entity states

| State | Meaning |
|---|---|
| transient | plain Java object, not tracked by Hibernate |
| managed | attached to persistence context; changes are tracked |
| detached | was managed earlier, but no longer tracked |
| removed | scheduled for delete |

The persistence context is Hibernate's first-level cache and unit of work, usually scoped to a transaction.

---

## Transient

```java
User user = new User();
user.setEmail("a@example.com");
```

This object is not connected to the database. Hibernate does not track changes.

It becomes managed after `persist()` or repository `save()` for a new entity.

---

## Managed

Managed means Hibernate is tracking the object.

```java
@Transactional
public void changeEmail(Long id, String email) {
    User user = userRepository.findById(id).orElseThrow();
    user.setEmail(email);
}
```

No explicit `save()` is required here. At transaction commit, Hibernate dirty checking detects the changed email and sends an update.

Important point:

> Dirty checking works only for managed entities inside an active persistence context.

---

## Detached

Detached means the object is no longer tracked.

```java
User user = userRepository.findById(id).orElseThrow();
// transaction ends

user.setEmail("new@example.com"); // not tracked
```

Common causes:

- transaction/session ended
- entity was returned outside the service layer
- `entityManager.detach(entity)` or `clear()` was called

To update a detached entity, load the managed entity again or merge it.

---

## Removed

Removed means Hibernate will delete the row on flush/commit.

```java
@Transactional
public void deleteUser(Long id) {
    User user = userRepository.findById(id).orElseThrow();
    userRepository.delete(user);
}
```

The delete is part of the transaction and happens at flush/commit.

---

## State transition

```text
new object
  ↓ persist/save new
managed
  ↓ transaction ends
detached
  ↓ merge/save existing
managed
  ↓ remove/delete
removed
  ↓ flush/commit
deleted row
```

---

## persist vs merge vs save

| Method | Use case | Important behavior |
|---|---|---|
| `persist()` | new entity | makes it managed |
| `merge()` | detached entity | copies state into a managed instance and returns it |
| `repository.save()` | Spring Data wrapper | uses persist for new, merge for existing |

Merge trap:

```java
User detached = new User();
detached.setId(10L);
detached.setEmail("new@example.com");

User managed = entityManager.merge(detached);

detached.setEmail("ignored@example.com"); // still detached
managed.setEmail("saved@example.com");    // tracked
```

Use the returned instance from `merge()` / `save()` when updating detached objects.

---

## Lifecycle callbacks

JPA callbacks let an entity run small lifecycle logic.

```java
@Entity
public class User {
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void beforeInsert() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void beforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
```

Common use cases:

- set `createdAt`
- set `updatedAt`
- normalize simple fields before save

Keep callbacks simple. Do not call repositories or external services from entity callbacks.

---

## LazyInitializationException

This happens when code accesses a lazy association after the persistence context is closed.

```java
Order order = orderRepository.findById(id).orElseThrow();
// transaction ends

order.getItems().size(); // LazyInitializationException
```

Fix by loading what you need inside the transaction:

```java
@Transactional(readOnly = true)
public OrderDetails getOrder(Long id) {
    Order order = orderRepository.findWithItems(id).orElseThrow();
    return OrderDetails.from(order);
}
```

Or use DTO projections for read-only API responses.

---

## Quick recall

**Q. What is a managed entity?**  
A. An entity attached to the persistence context; Hibernate tracks its changes.

**Q. Why no `save()` after changing a managed entity?**  
A. Dirty checking flushes changes at transaction commit.

**Q. What is detached?**  
A. An entity object that is no longer tracked by Hibernate.

**Q. What is the merge trap?**  
A. `merge()` returns a managed copy; the original object remains detached.

**Q. Common callback use case?**  
A. Setting `createdAt` and `updatedAt`.

**Q. Why does `LazyInitializationException` happen?**  
A. Lazy association is accessed after the transaction/session is closed.
