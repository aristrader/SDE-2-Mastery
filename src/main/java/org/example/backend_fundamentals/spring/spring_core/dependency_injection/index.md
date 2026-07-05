---
order: 40
---

# Dependency Injection

---

## The three injection styles

### Constructor injection

```java
@Service
public class OrderService {

    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    // @Autowired optional since Spring 4.3 when there's a single constructor
    public OrderService(PaymentGateway paymentGateway, NotificationService notificationService) {
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }
}
```

### Field injection

```java
@Service
public class OrderService {

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private NotificationService notificationService;
}
```

### Setter injection

```java
@Service
public class OrderService {

    private PaymentGateway paymentGateway;
    private NotificationService notificationService;

    @Autowired
    public void setPaymentGateway(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Autowired(required = false)  // marks dependency as optional
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

---

## Why constructor injection wins

**1. Mandatory dependencies explicit at compile time.**
If `PaymentGateway` is missing, the code won't compile. With field injection, the compiler is silent — the NPE shows up at runtime, possibly deep inside a call chain.

**2. Object is always fully initialized.**
By the time the constructor returns, all dependencies are set. No window where a half-constructed bean is visible to another thread.

**3. `final` fields — immutability.**
Constructor-injected fields can be `final`. Immutability eliminates entire classes of threading bugs and is the recommended style for service-layer beans that should be stateless.

**4. Testable without Spring.**
```java
// No Spring context needed — pure unit test
OrderService service = new OrderService(mockPaymentGateway, mockNotificationService);
```
Field-injected classes require a Spring context or reflection-based test helpers (`ReflectionTestUtils`) to inject mocks. Constructor injection collapses this to a plain `new`.

**5. Spring itself recommends it since 4.3.**
The Spring team deprecated field injection in their own documentation. Single-constructor classes no longer need `@Autowired` — Spring detects and uses the only constructor automatically.

---

## Why field injection is problematic

- **Breaks immutability** — fields cannot be `final`.
- **Hides dependencies** — a class with 8 `@Autowired` fields doesn't advertise its coupling. A constructor with 8 parameters screams "this class is doing too much" — which is the right signal.
- **Can't unit-test without Spring** — no way to inject mocks through the public API; must use reflection.
- **`@Autowired` on a `final` field is a compile error** — Spring cannot set a `final` field after construction; it will refuse.
- Widely considered a code smell in contemporary Spring codebases.

---

## When setter injection is legitimate

Setter injection is appropriate in two narrow cases:

1. **Optional dependencies.** `@Autowired(required = false)` on a setter — the setter is only called if the bean exists. Check for null before use.
2. **Circular dependency as a last resort.** If two beans each require the other at construction time, one side can use setter injection so Spring can first instantiate both, then wire the setter. This is a design smell — prefer breaking the cycle with an intermediate service or event.

---

## @Autowired resolution order

When Spring resolves an `@Autowired` injection point it applies these rules in order: 40. **By type** — find all beans assignable to the declared type. If exactly one, done.
2. **`@Qualifier`** — if the injection point carries `@Qualifier("name")`, filter candidates to that specific bean name or qualifier. Overrides everything else.
3. **`@Primary`** — if multiple candidates remain and no `@Qualifier` was given, the bean marked `@Primary` wins.
4. **By name** — last resort: if still multiple candidates, match the field/parameter name against bean names.

If none of these produces exactly one candidate: `NoUniqueBeanDefinitionException`.

```java
@Autowired
@Qualifier("stripeGateway")
private PaymentGateway paymentGateway;  // ignores all other PaymentGateway beans
```

> Common interview trap: the order is often misquoted as "type → name → Qualifier → Primary". The correct order puts `@Qualifier` before `@Primary`, and name-matching is the fallback of last resort, not step 2.

---

## ObjectProvider — lazy and optional injection

`ObjectProvider<T>` is Spring's preferred way to inject a bean lazily or optionally without a full `ApplicationContext` reference.

```java
@Service
public class ReportService {

    private final ObjectProvider<HeavyAuditLogger> auditLoggerProvider;

    public ReportService(ObjectProvider<HeavyAuditLogger> auditLoggerProvider) {
        this.auditLoggerProvider = auditLoggerProvider;
    }

    public void generate() {
        // resolved on first call, not at construction time
        auditLoggerProvider.ifAvailable(logger -> logger.log("report generated"));
    }
}
```

Key methods:
- `getObject()` — resolves now; throws if none exists (like `getBean()`)
- `getIfAvailable()` — returns `null` if no bean exists; never throws
- `getIfUnique()` — returns `null` if zero or more than one candidate
- `ifAvailable(Consumer<T>)` — run the lambda only if a bean exists

Use `ObjectProvider` in preference to `@Autowired(required = false)` on a field — it's explicit about optionality and works with constructor injection, preserving immutability.

---

## Circular dependency

### With constructor injection — fails fast

```
BeanA depends on BeanB (via constructor)
BeanB depends on BeanA (via constructor)
→ BeanCurrentlyInCreationException at startup
```

Spring cannot construct `BeanA` without `BeanB`, nor `BeanB` without `BeanA`. It detects the cycle and throws immediately. This is the right outcome — circular dependencies are a design flaw and you want to know at startup.

### With field injection — masked at runtime

Spring resolves field-injected circular dependencies by creating one bean without its dependencies first (leaving fields null), injecting the partially-constructed proxy into the other bean, then backfilling. The beans "work" at runtime but:

- One bean is momentarily in an inconsistent state during wiring.
- The cycle is hidden — it won't be caught until you switch to constructor injection.
- It can interact badly with AOP proxies.

### Breaking a constructor circular dep without redesigning

If a redesign is not immediately possible, two escape hatches exist:

**Option 1 — setter injection on one side.** Convert one side's dependency to a setter. Spring constructs both beans first, then wires the setter, so the cycle no longer blocks instantiation.

**Option 2 — `@Lazy` on one constructor parameter.** Spring injects a CGLIB proxy placeholder; the real bean is resolved on first method call.

```java
@Service
public class BeanA {
    private final BeanB beanB;

    public BeanA(@Lazy BeanB beanB) {   // Spring injects a proxy; BeanB resolves lazily
        this.beanB = beanB;
    }
}
```

Both options are workarounds that hide a coupling problem. **The right fix is to redesign.** Common patterns: introduce a third service both depend on, use `ApplicationEvent` to decouple, or restructure responsibilities so the cycle cannot exist.

---

## Quick recall

**Q. Why can't you unit-test a field-injected class without Spring?**
A. There's no API to set the dependencies — they're private fields with no constructor or setter. You'd need `ReflectionTestUtils` or a Spring test context.

**Q. What is the `@Autowired` resolution order?**
A. Type → `@Qualifier` → `@Primary` → name (by field/parameter name). `@Qualifier` beats `@Primary`; name-match is the last resort. Ambiguity throws `NoUniqueBeanDefinitionException`.

**Q. Constructor injection and `final` — what's the connection?**
A. Constructor-injected fields can be declared `final`, enforcing immutability. Field-injected fields cannot be `final` — Spring sets them after construction via reflection.

**Q. When does a circular dependency become a `BeanCurrentlyInCreationException`?**
A. Only with constructor injection. Spring detects it at startup and fails immediately. Field injection masks cycles by wiring partially-constructed beans.

**Q. What is the legitimate use case for setter injection?**
A. Optional dependencies (`@Autowired(required = false)`) and, as a last resort, breaking circular dependencies — though the real fix is redesigning to remove the cycle.

**Q. Since when is `@Autowired` optional on a constructor?**
A. Since Spring 4.3 — when a class has exactly one constructor, Spring autowires it automatically without the annotation.

**Q. What is `ObjectProvider<T>` and when do you use it?**
A. A Spring wrapper for lazy or optional injection. Use `getIfAvailable()` / `ifAvailable()` when the dependency may not exist, instead of `@Autowired(required = false)` on a field — compatible with constructor injection.

**Q. How does `@Lazy` break a constructor circular dependency?**
A. Spring injects a CGLIB proxy instead of the real bean at construction time. The real bean is resolved on the first method call. It's a workaround — prefer redesigning to remove the cycle.
