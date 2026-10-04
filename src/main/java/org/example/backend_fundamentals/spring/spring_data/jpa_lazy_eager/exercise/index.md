---
order: 10
search: false
---

# Exercises: Lazy vs Eager Loading & N+1 Query Optimization

Practice these scenario-based design and troubleshooting exercises to master JPA fetch strategies, transaction boundary mechanics, and database query optimization for senior backend engineering interviews.

---

## Exercise: triage-paginated-n-plus-one - Mitigating N+1 in Paginated Order History

### Scenario
An e-commerce order service exposes a paginated API endpoint `GET /api/v1/orders?page=0&size=20`. Under production load, database query count alerts fire because loading 20 orders results in 41 queries: 1 query to fetch the paginated orders, 20 queries to fetch each order's `List<OrderItem>`, and 20 queries to fetch each order's `Customer`.

To fix this, a developer wrote the following Spring Data JPA repository method:

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.customer
        JOIN FETCH o.items
        JOIN FETCH o.discounts
        """)
    Page<Order> findRecentOrders(Pageable pageable);
}
```

### Requirements & Questions
1. **Analyze the Pagination Failure**: What critical issue occurs when Hibernate executes a `JOIN FETCH` on a collection association (`o.items`) alongside a `Pageable` argument? Explain the runtime behavior and memory implications of log warning `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!`.
2. **Explain the Bag Fetch Constraint**: Why will Hibernate throw a `MultipleBagFetchException` on application startup or query execution for this repository method, and what underlying relational mechanism causes it?
3. **Propose the Production Architecture**: Design a robust, two-tier querying strategy or configuration that enables true database-level pagination (`LIMIT` and `OFFSET` in SQL) while eliminating N+1 queries for both `customer`, `items`, and `discounts`.

---

## Exercise: resolve-lazy-init-after-osiv-disable - Eliminating LazyInitializationException Post OSIV Teardown

### Scenario
To eliminate connection pool exhaustion under high traffic, your platform engineering team changed `application.yml` to:

```yaml
spring:
  jpa:
    open-in-view: false
```

Immediately after deployment, several read endpoints in the catalog service began returning HTTP 500 errors with the stack trace:
`org.hibernate.LazyInitializationException: could not initialize proxy [com.example.catalog.Category#42] - no Session`.

The problematic service and controller code is structured as follows:

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }
}

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow();
    }
}
```

The `Product` entity contains `@ManyToOne(fetch = FetchType.LAZY) private Category category;` and `@OneToMany(fetch = FetchType.LAZY) private List<Review> reviews;`.

### Requirements & Questions
1. **Trace the Root Cause**: Step through the thread execution and lifecycle boundaries of the `EntityManager`, `@Transactional`, and the Jackson `ObjectMapper` to explain why this exception did not occur under OSIV but surfaced immediately once OSIV was disabled.
2. **Design the Remediation Plan**: Compare two distinct solutions without re-enabling OSIV:
   - Approach A: Dynamic fetch plans using `@EntityGraph` paired with Service-layer DTO conversion.
   - Approach B: Direct JPQL constructor DTO projections (`SELECT new ...`).
3. **Evaluate Trade-offs**: Under what circumstances (e.g., read-only throughput vs write/mutation workflows) should an engineering team prefer Approach A vs Approach B?

---

## Exercise: non-owning-one-to-one-leak - Diagnosing the Non-Owning @OneToOne Eager Query Leak

### Scenario
In a core identity microservice, `User` and `UserProfile` entities have a bidirectional `@OneToOne` relationship.

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;

    private String username;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UserProfile profile;
}

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String bio;
}
```

When invoking `userRepository.findAll()`, the backend logs demonstrate 1 query to fetch all users, followed immediately by individual `SELECT` queries to `user_profiles` for each user row, despite `fetch = FetchType.LAZY` being explicitly declared on `User.profile`.

### Requirements & Questions
1. **Explain the Proxy Limitation**: Why does Hibernate ignore `FetchType.LAZY` on the non-owning side of a `@OneToOne` association when using standard Java dynamic proxies?
2. **Identify Ownership Mechanics**: How does the presence or absence of the foreign key column in the underlying relational table dictate whether Hibernate can safely construct an uninitialized proxy?
3. **Specify Two Architectural Fixes**: Provide two concrete ways to fix this behavior so that querying `User` does not trigger secondary queries for `UserProfile`.
