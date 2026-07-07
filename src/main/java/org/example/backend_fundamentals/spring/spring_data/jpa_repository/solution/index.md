---
order: 20
search: false
---

# JPA Repository Solutions

## Solution: basic-entity-mapping - Basic entity mapping
```java
@Entity
@Table(name = "transactions")
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String referenceId;

    @Column(nullable = false)
    private String status;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

## Solution: derived-queries - Derived queries
```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByStatus(String status);
    List<Transaction> findByAmountGreaterThan(BigDecimal amount);
    List<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    boolean existsByReferenceId(String referenceId);
    List<Transaction> findTop10ByStatusOrderByCreatedAtDesc(String status);
}
```

## Solution: query-modifying - @Query with @Modifying
```java
@Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.amount >= :minAmount")
List<Transaction> findByStatusAndMinAmount(@Param("status") String status, @Param("minAmount") BigDecimal minAmount);

@Modifying
@Transactional
@Query("UPDATE Transaction t SET t.status = :newStatus WHERE t.referenceId = :refId")
int updateStatusByReferenceId(@Param("refId") String refId, @Param("newStatus") String newStatus);
```

## Solution: calling-from-service - Calling from a service
```java
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;

    @Transactional
    public Transaction createTransaction(String referenceId, BigDecimal amount) {
        Transaction t = new Transaction();
        // setters...
        return repository.save(t);
    }
}
```

## Solution: transient-vs-transient - @Transient vs transient
```java
@Transient
private boolean highValueCache;

private transient String tempWorkingBuffer;
```
`@Transient` prevents JPA from mapping the field to a database column. The Java `transient` keyword prevents Java serialization. They are independent.
