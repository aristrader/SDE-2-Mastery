---
order: 10
search: false
---

# Dependency Injection Practice

## Why this matters
Constructor injection is the Spring team's recommended wiring style for exactly the reasons you hit on a KYC platform: services have 5-8 mandatory dependencies, and field injection hides them all. Getting this pattern into muscle memory means you never write an untestable service again — every dependency is visible at the call site, and unit tests spin up in milliseconds with no Spring context.

## Domain model

```java
// The collaborators a real KYC service would depend on.
// You'll wire these in the exercises below.

public interface UserRepository {
    Optional<User> findById(String userId);
}

public interface DocumentValidator {
    boolean validate(String documentType, byte[] content);
}

// Optional collaborator — may not be configured in all environments.
public interface NotificationService {
    void sendAlert(String userId, String message);
}
```

---

## Exercise 1: Wire a service with @RequiredArgsConstructor (~10 min)

**Goal:** Write a properly wired Spring service with zero boilerplate constructor code.

**Task:**
1. Create `KycVerificationService` annotated `@Service`.
2. Declare two `private final` fields: `UserRepository userRepository` and `DocumentValidator documentValidator`.
3. Annotate the class with `@RequiredArgsConstructor` (Lombok).
4. Add a method `boolean verify(String userId, String docType, byte[] content)` that calls both collaborators (stub bodies are fine).
5. Confirm there is NO `@Autowired` annotation anywhere in the class.

**Gotcha:** `@RequiredArgsConstructor` only generates a constructor for `final` and `@NonNull` fields. Non-final fields are silently skipped — they will be `null` at runtime. Always pair `@RequiredArgsConstructor` with `final`.

---

## Exercise 2: Unit test without Spring (~10 min)

**Goal:** Prove that constructor-injected services are trivially testable without a Spring context.

**Task:**
1. Create a plain Java class (no `@SpringBootTest`, no `@ExtendWith`) — even a `main` method is fine for this exercise.
2. Create stub implementations of `UserRepository` and `DocumentValidator` as anonymous classes or simple lambdas.
3. Instantiate `KycVerificationService` directly: `new KycVerificationService(stubRepo, stubValidator)`.
4. Call `verify(...)` and print the result.

**Gotcha:** With field injection (Exercise 3 below) you cannot do this — you'd need Mockito's `@InjectMocks` or a Spring test context. Constructor injection removes that dependency entirely.

---

## Exercise 3: Compare field injection (~10 min)

**Goal:** Feel the pain of field injection to understand what constructor injection prevents.

**Task:**
1. Write `KycVerificationServiceV2` — same logic as Exercise 1 but using `@Autowired` on each field (no `final`, no Lombok constructor annotation).
2. Try to write the same plain-Java test from Exercise 2 for `V2`. Note that you cannot: the fields are private, there is no constructor that accepts them, and the default no-arg constructor leaves them null.
3. Write a comment block in the file listing the three costs: (a) fields can't be `final`, (b) unit test requires Spring or Mockito magic, (c) missing dependencies are only detected at runtime, not compile time.

**Gotcha:** Field injection works fine in a running Spring app — the cost is purely testability and an obscured dependency graph. The pain surfaces during refactoring and onboarding, not in greenfield happy-path code.

---

## Exercise 4: Optional dependency via setter injection (~10 min)

**Goal:** Learn when setter injection is the right tool — for truly optional collaborators.

**Task:**
1. Add `NotificationService notificationService` as a non-final field in `KycVerificationService` (not in the constructor — it's optional).
2. Add a setter: `@Autowired(required = false) public void setNotificationService(NotificationService svc) { this.notificationService = svc; }`.
3. In the `verify(...)` method, guard the call: `if (notificationService != null) { notificationService.sendAlert(...); }`.
4. In your plain-Java test, construct the service without a `NotificationService` and confirm `verify(...)` still runs (no NPE).

**Gotcha:** `@Autowired(required = false)` means Spring skips injection if no bean of that type exists — it does NOT mean the field gets a default. The field starts `null` and stays `null` if nothing is wired. Always null-check before use.

---

## Exercise 5: @Primary and @Qualifier (~15 min)

**Goal:** Resolve ambiguous beans without changing the dependent service.

**Task:**
1. Create two implementations: `BasicDocumentValidator` and `AiDocumentValidator`, both implementing `DocumentValidator`. Annotate both with `@Component`.
2. Mark `BasicDocumentValidator` with `@Primary`.
3. Create a second service `HighRiskKycService` that explicitly injects `AiDocumentValidator` using `@Qualifier("aiDocumentValidator")` in its constructor parameter.
4. Confirm `KycVerificationService` (from Exercise 1) still gets `BasicDocumentValidator` automatically (no `@Qualifier` needed there).

**Gotcha:** The `@Qualifier` value defaults to the bean name, which defaults to the uncapitalised class name (`aiDocumentValidator`). Rename the class and the qualifier breaks silently at startup. Prefer a named constant or explicit `@Component("name")` to avoid this fragility.

---

## Circular dependency — what happens and how to fix it

If `ServiceA` requires `ServiceB` in its constructor and `ServiceB` requires `ServiceA`, Spring fails at startup with `BeanCurrentlyInCreationException` — caught eagerly at boot, not at first use. A feature, not a bug.

Two fixes:
1. **`@Lazy` on one constructor parameter** — Spring injects a proxy and resolves the real bean only on first call.
2. **Convert one dependency to setter injection** — break the cycle by wiring one collaborator after construction.

The cleaner long-term fix is to refactor: extract a third component both services depend on instead of depending on each other.

---

## Quick recall

**Q.** Why pair `@RequiredArgsConstructor` with `final` fields?
**A.** Lombok only generates the constructor for `final` (and `@NonNull`) fields — non-final fields are silently excluded and will be `null`.

**Q.** Since Spring 4.3, when can you omit `@Autowired` on a constructor?
**A.** When the class has exactly one constructor — Spring injects it automatically.

**Q.** How does Spring inject constructor dependencies — does it use reflection on private fields?
**A.** No. Spring calls the constructor directly. Dependencies become `final` the moment the constructor returns; there is no reflective field-writing after the fact.

**Q.** What are the two benefits of declaring injected fields `final`?
**A.** Immutability (the field can never be reassigned) and testability (you can construct the object in plain Java with `new Service(dep1, dep2)`).

**Q.** What exception does Spring throw for a circular constructor dependency, and when?
**A.** `BeanCurrentlyInCreationException`, thrown at application startup — not at first use.

**Q.** How do you break a constructor-injection circular dependency?
**A.** Add `@Lazy` to one constructor parameter (Spring injects a proxy), or convert one side to setter injection. The root fix is to refactor the cycle away entirely.

**Q.** What is the main testability advantage of constructor injection over field injection?
**A.** You can instantiate the class in plain Java (`new Service(dep1, dep2)`) with no Spring context or reflection tricks.

**Q.** When is setter injection appropriate instead of constructor injection?
**A.** For genuinely optional dependencies where the service must function even when the collaborator is absent.

**Q.** If two beans implement the same interface, how does Spring decide which to inject?
**A.** It prefers the `@Primary` bean; use `@Qualifier("beanName")` at the injection point to override that default.
