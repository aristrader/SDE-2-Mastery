# Spring Bean Injection & Qualifiers

A tour of how Spring resolves constructor-injected dependencies, focused on the `@Qualifier` / `@Primary` / parameter-name rules that decide *which* bean you get when multiple match.

Jumping-off example: the `traceableExecutorService` bean in `VidaClientConfiguration`.

```java
@Bean
public ExecutorService traceableExecutorService() {
    return ContextExecutorService.wrap(
        Executors.newFixedThreadPool(threadPoolSize),
        ContextSnapshotFactory.builder().build()::captureAll);
}
```

---

## 1. What a bean is, and how it gets named

A **bean** is an object whose lifecycle Spring manages. Two identifying properties matter for injection:

1. **Type(s)** — the class and every interface/superclass it implements. One bean matches many types.
2. **Name** — a unique string in the context. Two beans can share a type, never a name.

Default names:

| Declaration | Default bean name |
|---|---|
| `@Service public class OrderService` | `orderService` (class name, first letter lowercased) |
| `@Bean public ExecutorService traceableExecutorService()` | `traceableExecutorService` (method name) |
| `@Bean("foo") public ExecutorService traceableExecutorService()` | `foo` (explicit override) |

So the bean above is registered as **name** `traceableExecutorService`, **types** `ExecutorService`, `Executor`, `AutoCloseable`, and the concrete wrapper class.

---

## 2. The default: autowiring by type

Constructor injection matches **by type** first:

```java
@Service
public class StageExecutor {
    private final ExecutorService executor;

    public StageExecutor(ExecutorService executor) {   // matched by type
        this.executor = executor;
    }
}
```

Spring scans the context for beans assignable to `ExecutorService`. Three outcomes:

| Matches | Result |
|---|---|
| 0 | `NoSuchBeanDefinitionException` at startup |
| 1 | Inject it. Parameter name is irrelevant. |
| 2+ | Disambiguate (next section). Fails at startup if ambiguous. |

Ambiguity always fails at **startup**, never at runtime.

---

## 3. The disambiguation ladder

When multiple beans match, Spring runs down this list and stops at the first step that resolves to exactly one bean:

1. **`@Primary`** — one bean marked as the default.
2. **Parameter name matches a bean name** — requires compiler `-parameters` flag.
3. **`@Qualifier("name")`** — explicit selection at the injection point.
4. **Fail** — `NoUniqueBeanDefinitionException` at startup.

### `@Primary`

Marks one bean as the default for its type:

```java
@Bean @Primary
public ExecutorService traceableExecutorService() { ... }

@Bean
public ExecutorService reportingExecutor() { ... }
```

Now `ExecutorService executor` injects `traceableExecutorService` without any annotation.

Use when one bean is the dominant default and the others are exceptions. Don't use when multiple beans are peers — it just hides which one is picked.

### Parameter-name matching

```java
public StageExecutor(ExecutorService traceableExecutorService) {
    this.executor = traceableExecutorService;
}
```

Spring sees parameter name `traceableExecutorService`, finds a bean with that name, uses it. Zero annotations needed — but relies on the compiler preserving parameter names (`-parameters` flag). Spring Boot enables it by default.

Fragility: IDE renames or build-config changes can silently break the wiring.

### `@Qualifier`

Explicit selection:

```java
public StageExecutor(
    @Qualifier("traceableExecutorService") ExecutorService executor) {
    this.executor = executor;
}
```

Field name is now irrelevant. Most durable option — survives IDE renames and compiler-flag changes.

---

## 4. `@Primary` vs `@Qualifier` — when to use which

| Situation | Use |
|---|---|
| One bean is the dominant default, others are exceptions | `@Primary` on the default, `@Qualifier` on the exception call sites |
| Multiple peers, no natural default | `@Qualifier` on every call site |
| Only one bean today, future-proofing | Parameter name match or `@Qualifier`. Skip `@Primary`. |
| Test double replacing a prod bean | `@Primary` on the test bean |

Example of the "one primary, many peers" pattern:

```java
@Bean @Primary
public ExecutorService defaultExecutor() { ... }

@Bean
public ExecutorService reportingExecutor() { ... }
```

```java
public StageExecutor(ExecutorService executor) { ... }           // gets default
public ReportService(@Qualifier("reportingExecutor") ExecutorService e) { ... }
```

---

## 5. `@Autowired`, `@Inject`, `@Resource` — the three annotations

- **`@Autowired`** (Spring) — the standard one. On a single-constructor class in Spring 4.3+, it's optional. With Lombok's `@RequiredArgsConstructor`, never needed.
- **`@Inject`** (`jakarta.inject.Inject`) — JSR-330 standard equivalent. Use only for cross-framework portability. In a Spring-only codebase, prefer `@Autowired`.
- **`@Resource`** (`jakarta.annotation.Resource`) — matches **by name first**, type second. `@Resource(name = "x")` or uses the field name if unspecified. Legacy — prefer `@Qualifier + @Autowired`.

In this codebase, the default pattern:

```java
@Service
@RequiredArgsConstructor   // Lombok generates a single constructor
public class StageExecutor {
    private final ExecutorService executor;   // no @Autowired needed
}
```

### Lombok + `@Qualifier` gotcha

Lombok doesn't copy `@Qualifier` from fields to generated constructor parameters by default. Two options:

**Option A — write the constructor manually:**

```java
@Service
public class StageExecutor {
    private final ExecutorService executor;

    public StageExecutor(
        @Qualifier("traceableExecutorService") ExecutorService executor) {
        this.executor = executor;
    }
}
```

**Option B — tell Lombok to copy `@Qualifier`:**

```properties
# lombok.config at project root
lombok.copyableAnnotations += org.springframework.beans.factory.annotation.Qualifier
```

Now `@Qualifier` on a field propagates to the generated constructor.

---

## 6. The `-parameters` compiler flag

The parameter-name step of the disambiguation ladder requires that compiled `.class` files retain parameter names. By default `javac` strips them; `-parameters` preserves them. Spring Boot's Maven/Gradle plugins enable it automatically.

Quick check:

```bash
javap -v YourClass.class | grep MethodParameters
```

If it shows real names, the flag is on. If you only see `arg0`, `arg1`, it isn't.

**Implication**: parameter-name-based disambiguation works here, but isn't portable. `@Qualifier` is always safe.

---

## 7. Custom qualifier annotations

`@Qualifier("traceableExecutorService")` uses a magic string — typos are caught only at startup. For heavier use, define a custom qualifier:

```java
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Qualifier    // meta-annotation — makes this a qualifier
public @interface Traceable {}
```

Tag both producer and consumer:

```java
@Bean @Traceable
public ExecutorService traceableExecutorService() { ... }

public StageExecutor(@Traceable ExecutorService executor) { ... }
```

Benefits: no magic strings, refactor-safe, self-documenting. Overkill for one bean — reach for it when you have 3+ beans of the same type distinguished by a semantic property.

---

## Applying it to the original question

```java
@Bean
public ExecutorService traceableExecutorService() { ... }
```

Only one `ExecutorService` bean exists in this codebase today. Every form below works:

```java
// 1. Field name = bean name — works today, survives adding a second bean later
public StageExecutor(ExecutorService traceableExecutorService) { ... }

// 2. Generic field name — works today, breaks if a second bean is added
public StageExecutor(ExecutorService executorService) { ... }

// 3. Explicit qualifier — robust against future bean additions
public StageExecutor(@Qualifier("traceableExecutorService") ExecutorService executor) { ... }
```

Ranking:

1. **Field name = bean name** (option 1) — zero ceremony, makes the role obvious. Good default.
2. **`@Qualifier`** (option 3) — use when the field name wants to be shorter, or when you want immunity to future bean additions.
3. **Generic name** (option 2) — avoid. Works only while there's exactly one bean, and this bean's context-propagation property is easy to lose silently.

---

## Vocabulary recap

| Term | Meaning |
|---|---|
| **Bean** | An object whose lifecycle Spring manages. |
| **ApplicationContext** | The registry of all beans. |
| **Autowiring** | Spring's automatic matching of a dependency to a bean. |
| **Injection point** | Constructor parameter, field, or setter that Spring fills in. |
| **`@Autowired`** | Spring's "inject a bean here." Optional on single-constructor classes. |
| **`@Qualifier`** | Narrows bean selection among same-type candidates. |
| **`@Primary`** | Marks one bean as the default when multiple exist. |
| **Custom qualifier** | An annotation meta-annotated with `@Qualifier` — type-safe alternative to string names. |
| **`NoSuchBeanDefinitionException`** | Zero beans matched. Startup failure. |
| **`NoUniqueBeanDefinitionException`** | Multiple matched, none preferred. Startup failure. |
| **`-parameters`** | `javac` flag preserving parameter names in `.class` files. Required for parameter-name disambiguation. Spring Boot enables it by default. |
