---
order: 20
---

# JPA Repository Hierarchy

Spring Data JPA repositories remove most boilerplate CRUD code. For interviews, focus on what each repository level gives, where transactions should live, and the practical difference between `save`, `saveAndFlush`, `findById`, and `getReferenceById`.

---

## Repository hierarchy

```text
Repository
  └── CrudRepository
        └── PagingAndSortingRepository
              └── JpaRepository
```

| Interface | What it gives |
|---|---|
| `Repository` | marker interface |
| `CrudRepository` | `save`, `findById`, `findAll`, `delete`, `count`, `exists` |
| `PagingAndSortingRepository` | paging and sorting methods |
| `JpaRepository` | JPA-specific methods like `flush`, `saveAndFlush`, batch deletes, `getReferenceById` |

Most Spring Boot applications use `JpaRepository` directly.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

Here:

- `User` is the JPA entity managed by this repository.
- `Long` is the type of the entity's primary key field.

```java
@Entity
public class User {
    @Id
    private Long id;

    private String email;
}
```

---

## Runtime implementation

You write an interface:

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}
```

Spring creates the implementation at runtime.

Interview-level answer:

> Spring Data creates a proxy for the repository interface. Standard methods are backed by `SimpleJpaRepository`; derived query methods are parsed from method names.

---

## Transactions belong at service layer

Repository methods have transactional behavior, but the service method should usually define the real business transaction.

Bad boundary:

```java
public void placeOrder(Order order) {
    orderRepository.save(order);       // transaction 1
    paymentRepository.save(payment);   // transaction 2
}
```

Better:

```java
@Service
public class OrderService {

    @Transactional
    public void placeOrder(CreateOrderRequest request) {
        Order order = orderRepository.save(new Order(request));
        paymentRepository.save(Payment.forOrder(order));
    }
}
```

Why:

- one business use case commits or rolls back together
- fewer per-call transactions
- Hibernate first-level cache works across the full use case

Repository transactions are a safety net, not the best business boundary.

---

## save() means persist or merge

`save()` behaves differently depending on whether the entity is new.

```java
User user = new User();
user.setEmail("a@example.com");

User saved = userRepository.save(user);
```

For a new entity, Spring Data calls `EntityManager.persist`.

For an existing/detached entity, it calls `EntityManager.merge`.

```java
User detached = new User();
detached.setId(10L);
detached.setEmail("new@example.com");

User managed = userRepository.save(detached);
```

Important trap:

> With merge, the returned object is the managed one. Keep using the returned reference.

If IDs are manually assigned, Spring may think the object is existing and call `merge`, causing an unnecessary select. In that case, implement `Persistable` only if you really need custom “new entity” detection.

---

## save vs saveAndFlush

| Method | What happens |
|---|---|
| `save()` | persists changes; SQL is usually sent at flush/commit |
| `saveAndFlush()` | saves and immediately flushes SQL to the database |

Normal code should use `save()`.

Use `saveAndFlush()` only when something inside the same transaction must see the database row immediately, for example:

- calling a stored procedure after saving
- running a native query that must read the inserted row
- a test that must assert DB state before transaction commit

Do not use `saveAndFlush()` everywhere. It adds extra database round trips and can reduce batching.

---

## findById vs getReferenceById

| Method | Behavior | Use when |
|---|---|---|
| `findById(id)` | immediately queries DB and returns `Optional<T>` | you need the entity data |
| `getReferenceById(id)` | returns a lazy proxy; may not query immediately | you only need a foreign-key reference |

Example: assigning an existing author to a new book.

```java
Book book = new Book();
book.setTitle("Spring Notes");
book.setAuthor(authorRepository.getReferenceById(authorId));

bookRepository.save(book);
```

This can avoid selecting the author row just to set the foreign key.

If the referenced row does not exist, the failure may happen later when the proxy is accessed or when the FK constraint is checked.

---

## Quick recall

**Q. Which repository interface is commonly used in Spring Data JPA?**  
A. `JpaRepository`.

**Q. What does Spring create for repository interfaces?**  
A. A runtime proxy backed by Spring Data JPA implementation code.

**Q. Where should `@Transactional` usually be placed?**  
A. On service methods that represent business use cases.

**Q. `save()` on new vs existing entity?**  
A. New entity uses `persist`; existing/detached entity uses `merge`.

**Q. `save()` vs `saveAndFlush()`?**  
A. `save()` is normal. `saveAndFlush()` forces SQL immediately and should be rare.

**Q. `findById` vs `getReferenceById`?**  
A. `findById` loads now. `getReferenceById` gives a proxy, useful for FK assignment.
