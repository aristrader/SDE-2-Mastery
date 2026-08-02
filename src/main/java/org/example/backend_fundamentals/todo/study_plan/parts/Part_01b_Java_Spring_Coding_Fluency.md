# Part 1b — Java & Spring Coding Fluency

> **Sprint allocation:** Week 2 (alongside Part 3 theory). **Budget: ~7 hrs for 🔴 rows; ~13 hrs for 🔴+🟠; full ~17 hrs.**
> **Purpose:** Exercise-only — no new theory. Every row is a coding exercise that translates reading into muscle memory. A row is **Done** when you can write the code from scratch in under the time budget without reference. If Part 3 covers a row's topic fully, mark it Done directly.

---

## 1b Coding fluency — exercise inventory

| # | Topic | Tags | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|---|----------|-------------|-------|-----------|
| 1 | Streams core — filter / map / flatMap / reduce / anyMatch / distinct / sorted | 🔴 💼 🎯 | 30 min | [x] | [ ] | [ ] | [ ] | Structured practice exists across intermediate/terminal stream exercises | 📖 `java/coding_fluency/streams/intermediate_operations/exercise/index.md` · 📖 `java/coding_fluency/streams/terminal_operations/exercise/index.md` |
| 2 | Stream collectors — groupingBy / toMap / joining / partitioningBy / counting downstream | 🔴 💼 🎯 | 45 min | [x] | [ ] | [ ] | [ ] | Structured practice created | 📖 `java/coding_fluency/streams/grouping/exercise/index.md` · 📖 `java/coding_fluency/streams/to_map/exercise/index.md` · 📖 `java/coding_fluency/streams/joining/exercise/index.md` · 📖 `java/coding_fluency/streams/advanced_collectors/exercise/index.md` |
| 3 | Method references — 4 forms (static ref, bound instance ref, unbound instance ref, constructor ref) | 🔴 💼 | 20 min | [x] | [ ] | [ ] | [ ] | Structured practice created | 📖 `java/coding_fluency/method_references/exercise/index.md` · 💻 Replace each lambda in a stream pipeline with the equivalent method reference — one of each form |
| 4 | Optional — orElse / orElseGet / map / flatMap / filter / ifPresent / orElseThrow | 🔴 💼 🎯 | 20 min | [x] | [ ] | [ ] | [ ] | Structured practice created | 📖 `java/coding_fluency/optional/exercise/index.md` · 💻 Rewrite a 3-level null-check chain as Optional; pick the right orElse* variant for each case (eager vs lazy) |
| 5 | Lombok — @Value / @Builder / @Data / @Slf4j / @RequiredArgsConstructor | 🔴 💼 | 30 min | [x] | [ ] | [ ] | [ ] | Structured practice created | 📖 `java/coding_fluency/lombok/exercise/index.md` · 💻 Immutable DTO with @Value + @Builder; @Slf4j in a service; compare @Data vs @Value — which allows mutation? |
| 6 | Records as DTOs — compact constructor validation / toBuilder pattern | 🔴 💼 | 20 min | [x] | [ ] | [ ] | [ ] | Structured practice created | 📖 `java/coding_fluency/records/exercise/index.md` · 💻 Define `record TransactionDto(String id, BigDecimal amount)` with compact ctor validation; use as API response body |
| 7 | Immutable collections — List.of / Map.of / Set.of / copyOf — mutation traps | 🔴 💼 | 20 min | [x] | [ ] | [ ] | [ ] | Structured practice created | 📖 `java/coding_fluency/immutable_collections/exercise/index.md` · 💻 Write 3 cases: List.of mutation (UnsupportedOperationException), Map.of null-key (NPE), copyOf proves independence from original |
| 8 | Constructor injection — @Component + final fields + @RequiredArgsConstructor | 🔴 💼 | 45 min | [x] | [ ] | [ ] | [ ] | Structured Spring DI practice exists | 📖 `spring/spring_core/dependency_injection/exercise/index.md` |
| 9 | JPA entity + repository — @Entity / @Id / @GeneratedValue / @Column / JpaRepository | 🔴 💼 | 45 min | [x] | [ ] | [ ] | [ ] | Structured JPA repository practice exists | 📖 `spring/spring_data/jpa_repository/exercise/index.md` |
| 10 | Feign client — @FeignClient / request mapping / @PathVariable / error decoder | 🔴 💼 | 45 min | [ ] | [x] | [ ] | [ ] | Partial: structured practice exists; needs one quick personal revision | 📖 `spring/spring_cloud/feign/exercise/index.md` |
| 11 | Global exception handling — @ControllerAdvice / @ExceptionHandler / MethodArgumentNotValidException | 🔴 💼 🎯 | 45 min | [x] | [ ] | [ ] | [ ] | Structured exception-handling practice exists | 📖 `spring/spring_web/exception_handling/exercise/index.md` |
| 12 | @ConfigurationProperties — binding nested YAML / @Validated / immutable record config | 🔴 💼 | 45 min | [ ] | [ ] | [ ] | [ ] | | 💻 Bind a multi-level YAML block to a @ConfigurationProperties record; add @NotNull validation; inject into a service |
| 13 | Functional interfaces — custom Predicate / Function composition / BiFunction | 🟠 💼 | 45 min | [ ] | [ ] | [ ] | [ ] | | 💻 Build a validation pipeline with Predicate.and / .or / .negate; compose Functions via .andThen / .compose |
| 14 | CompletableFuture — thenApply / thenCompose / thenCombine / exceptionally / allOf | 🟠 💼 | 45 min | [ ] | [ ] | [ ] | [ ] | | 💻 Call 2 async methods, combine results, handle one failure gracefully — no .get() / .join() inside the chain |
| 15 | Jackson — @JsonProperty / @JsonIgnore / custom serializer / ObjectMapper config | 🟠 💼 | 45 min | [ ] | [x] | [ ] | [ ] | Partial: structured practice exists; needs one quick personal revision | 📖 `spring/spring_web/jackson/exercise/index.md` |
| 16 | Bean validation — @Valid / @NotNull / @Size / custom @Constraint / @Validated on service method | 🟠 💼 | 45 min | [ ] | [x] | [ ] | [ ] | Partial: REST validation covered; custom constraint/service-method validation pending | 📖 `spring/spring_web/rest/exercise/index.md` |
| 17 | @Cacheable / @CacheEvict / @CachePut — key expressions / condition / unless | 🟠 💼 | 45 min | [ ] | [x] | [ ] | [ ] | Partial: structured practice exists; needs one quick personal revision | 📖 `spring/spring_core/caching/exercise/index.md` |
| 18 | WebClient — builder / retrieve / bodyToMono / onStatus / error mapping | 🟠 💼 | 45 min | [ ] | [ ] | [ ] | [ ] | | 💻 GET call → map response to DTO → map 4xx / 5xx to domain exceptions — non-blocking chain, no block() |
| 19 | Enums with behaviour — fields / abstract methods / interface impl / exhaustive switch | 🟠 💼 | 30 min | [x] | [ ] | [ ] | [ ] | Structured enum practice exists | 📖 `java/foundations/enums/exercise/index.md` · 💻 `TransactionStatus` enum with `boolean isFinal()` and abstract `String label()`; exhaustive switch over it |
| 20 | AOP — @Aspect / @Around / @Before / pointcut expressions / ProceedingJoinPoint | 🟠 💼 | 45 min | [ ] | [ ] | [ ] | [ ] | | 💻 Aspect that logs method entry/exit + elapsed time for all @Service methods; verify it fires in a test |
| 21 | JPA relationships — @OneToMany / @ManyToOne / FetchType / N+1 problem + fix | 🟠 💼 🎯 | 45 min | [ ] | [x] | [ ] | [ ] | Partial: structured lazy/eager/N+1 practice exists; needs one quick personal revision | 📖 `spring/spring_data/jpa_lazy_eager/exercise/index.md` |
| 22 | @Async + TaskDecorator — MDC propagation across thread boundaries | 🟠 💼 🎯 | 45 min | [ ] | [x] | [ ] | [ ] | Partial: structured async MDC practice exists; needs one quick personal revision | 📖 `spring/spring_core/async_mdc/exercise/index.md` |
| 23 | @Scheduled — fixedRate vs fixedDelay vs cron / dedicated ThreadPoolTaskScheduler | 🟡 💼 | 30 min | [ ] | [ ] | [ ] | [ ] | | 💻 2 scheduled jobs: one fixedRate (overlapping-safe), one cron; configure a dedicated scheduler bean |
| 24 | Switch expressions — arrow syntax / yield / type pattern matching | 🟡 | 30 min | [ ] | [x] | [ ] | [ ] | Partial: switch expression practice exists; type pattern matching pending | 📖 `java/foundations/control_flow/exercise/index.md` |
| 25 | MapStruct — @Mapper / @Mapping / nested mapping / NullValuePropertyMappingStrategy | 🟡 💼 | 45 min | [ ] | [ ] | [ ] | [ ] | | 💻 Entity → response DTO mapping; custom @Mapping for a date field; null handling strategy |
| 26 | Text blocks + var — multiline literals / type inference limits | 🟢 | 20 min | [ ] | [ ] | [ ] | [ ] | | 💻 JSON string constant → text block; convert 3 locals to var; find 1 case where var kills readability |
| 27 | Sealed classes + exhaustive pattern switch — permits / instanceof binding | 🟢 | 30 min | [ ] | [ ] | [ ] | [ ] | | 💻 `sealed interface Shape permits Circle, Rectangle, Triangle`; exhaustive switch; confirm compiler catches missing arm |

---

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (rows 1–12) | ~7 hrs | ~0.6 wk | |
| 🔴 + 🟠 HIGH (rows 1–21) | ~13.3 hrs | ~1.2 wk | |
| Full Part (all rows) | ~16.7 hrs | ~1.5 wk | |

> Estimates assume zero prior fluency with each API. Subtract heavily for anything you already write daily.

---

## Learning progression

```
Streams core (1)
  └── Stream collectors (2)
        └── Method references (3)

Optional (4)   Lombok (5)   Records (6)   Immutable collections (7)
                  │
                  ▼
          Constructor injection (8)
                  │
          JPA entity + repo (9)──────────► JPA relationships (21)
                  │                               │
          Feign client (10)              N+1 fix (21b)
                  │
          Global exception handling (11)
                  │
          @ConfigurationProperties (12)

Functional interfaces (13) ──► CompletableFuture (14)
Jackson (15) ──► Bean validation (16)
@Cacheable (17)   WebClient (18)   AOP (20)
```

> Work 🔴 rows in order — each builds on the last. 🟠 rows are independent of each other.

---

## Frequently asked — live coding scenarios

These are the kinds of exercises interviewers give in 30-45 min live coding sessions:

1. **"Write a service method that fetches a user, validates they can withdraw, and updates their balance — with proper exception handling."**  
   Tests: constructor DI, checked vs unchecked exceptions, @Transactional intuition.

2. **"Given this list of transactions, compute the total amount per currency using streams."**  
   Tests: groupingBy + summingDouble / reducing downstream, method references.

3. **"Our Feign client is returning 404 for unknown users. Map that to a domain exception."**  
   Tests: ErrorDecoder, exception hierarchy, @ControllerAdvice wiring.

4. **"Our scheduled job is losing the correlation ID when it runs. Fix it."**  
   Tests: @Async + TaskDecorator, MDC propagation, thread-local semantics.

5. **"Add caching to this service — cache by userId, evict when the user is updated."**  
   Tests: @Cacheable key SpEL, @CacheEvict, unless condition.

---

## Trick questions / gotchas

1. **`orElse` vs `orElseGet`** — `orElse(expensiveCall())` always evaluates `expensiveCall()`, even when the value is present. Use `orElseGet(() -> expensiveCall())` when the default is expensive.

2. **`toMap` duplicate keys** — `Collectors.toMap(Order::getId, o -> o)` throws `IllegalStateException` on duplicate keys. Always supply a merge function for real data.

3. **`@Async` on the same class** — calling an `@Async` method from within the same class bypasses the proxy → method runs synchronously. Must call through the Spring bean.

4. **`@Cacheable` self-invocation** — same proxy problem as @Async. Internal calls skip the cache.

5. **`@Transactional` + lazy loading** — accessing a lazy-loaded association outside the transaction throws `LazyInitializationException`. Fix: use JOIN FETCH, @EntityGraph, or keep access inside the transactional boundary.

6. **Lombok `@Builder` + `@Value`** — `@Value` makes all fields final + private. `@Builder` is compatible. `@Data` generates setters too — use `@Value` for immutable DTOs, not `@Data`.

7. **Jackson and records** — Jackson needs a constructor or `@JsonCreator` to deserialize records. Spring Boot 2.7+ adds this automatically; older versions need explicit config.

8. **`CompletableFuture.thenApply` vs `thenCompose`** — `thenApply` maps `T → U`. `thenCompose` maps `T → CompletableFuture<U>` (flatMap equivalent). Using `thenApply` with a function that returns a future gives `CompletableFuture<CompletableFuture<U>>`.

---

## Quick recall

**Q. Constructor injection vs field injection — why prefer constructor?**  
A. Constructor injection makes dependencies explicit, supports final fields (immutability), and lets you instantiate the class in tests without Spring. Field injection hides dependencies and forces Spring context in tests.

**Q. `orElse` vs `orElseGet` — one-line rule?**  
A. Use `orElseGet` when the default involves a method call — it's lazy and only runs if the Optional is empty. `orElse` always evaluates the argument.

**Q. When does `toMap` throw?**  
A. When two stream elements map to the same key and no merge function is provided — `IllegalStateException: Duplicate key`.

**Q. Four forms of method reference?**  
A. Static (`ClassName::staticMethod`), bound instance (`obj::method`), unbound instance (`ClassName::instanceMethod` — first stream element becomes the receiver), constructor (`ClassName::new`).

**Q. N+1 problem in one sentence — and the fix?**  
A. Loading a collection lazily inside a loop fires one query per element. Fix: JOIN FETCH in JPQL or `@EntityGraph` to eagerly join in a single query.

**Q. `@Async` and MDC — what breaks and why?**  
A. Spring's `@Async` runs the method on a different thread from a pool. Thread-locals (including MDC) aren't copied to pool threads. Fix: implement `TaskDecorator` that copies MDC before the task runs and clears after.

**Q. `@Cacheable` — what does `unless` do vs `condition`?**  
A. `condition` is evaluated before the method runs — if false, caching is skipped entirely. `unless` is evaluated after — the method still runs but the result isn't cached (useful for `unless = "#result == null"`).
