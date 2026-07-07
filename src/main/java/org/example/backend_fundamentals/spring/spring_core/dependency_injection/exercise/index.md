---
order: 10
search: false
---

# Dependency Injection Practice

## Domain model

```java
public interface UserRepository {
    Optional<User> findById(String userId);
}

public interface DocumentValidator {
    boolean validate(String documentType, byte[] content);
}

public interface NotificationService {
    void sendAlert(String userId, String message);
}
```

## Exercise: wire-requiredargsconstructor - Wire a service with @RequiredArgsConstructor

### Goal
Write a properly wired Spring service with zero boilerplate constructor code.

### Task
1. Create `KycVerificationService` annotated `@Service`.
2. Declare two `private final` fields: `UserRepository userRepository` and `DocumentValidator documentValidator`.
3. Annotate the class with `@RequiredArgsConstructor` (Lombok).
4. Add a method `boolean verify(String userId, String docType, byte[] content)` that calls both collaborators (stub bodies are fine).
5. Confirm there is NO `@Autowired` annotation anywhere in the class.

### Gotcha
`@RequiredArgsConstructor` only generates a constructor for `final` and `@NonNull` fields. Non-final fields are silently skipped — they will be `null` at runtime.

## Exercise: unit-test-no-spring - Unit test without Spring

### Goal
Prove that constructor-injected services are trivially testable without a Spring context.

### Task
1. Create a plain Java class (no `@SpringBootTest`, no `@ExtendWith`).
2. Create stub implementations of `UserRepository` and `DocumentValidator` as anonymous classes or simple lambdas.
3. Instantiate `KycVerificationService` directly: `new KycVerificationService(stubRepo, stubValidator)`.
4. Call `verify(...)` and print the result.

### Gotcha
With field injection you cannot do this — you'd need Mockito's `@InjectMocks` or a Spring test context. Constructor injection removes that dependency entirely.

## Exercise: compare-field-injection - Compare field injection

### Goal
Feel the pain of field injection to understand what constructor injection prevents.

### Task
1. Write `KycVerificationServiceV2` — same logic as Exercise 1 but using `@Autowired` on each field.
2. Try to write the same plain-Java test from Exercise 2 for `V2`. Note that you cannot: the fields are private, there is no constructor that accepts them, and the default no-arg constructor leaves them null.
3. Write a comment block in the file listing the three costs: (a) fields can't be `final`, (b) unit test requires Spring or Mockito magic, (c) missing dependencies are only detected at runtime, not compile time.

### Gotcha
Field injection works fine in a running Spring app — the cost is purely testability and an obscured dependency graph.

## Exercise: optional-dependency-setter - Optional dependency via setter injection

### Goal
Learn when setter injection is the right tool — for truly optional collaborators.

### Task
1. Add `NotificationService notificationService` as a non-final field in `KycVerificationService`.
2. Add a setter: `@Autowired(required = false) public void setNotificationService(NotificationService svc) { this.notificationService = svc; }`.
3. In the `verify(...)` method, guard the call: `if (notificationService != null) { ... }`.
4. In your plain-Java test, construct the service without a `NotificationService` and confirm `verify(...)` still runs (no NPE).
5. Confirm `KycVerificationService` from Exercise 1 still gets `BasicDocumentValidator` automatically without a `@Qualifier`.

### Gotcha
`@Autowired(required = false)` means Spring skips injection if no bean of that type exists — it does NOT mean the field gets a default. The field starts `null` and stays `null`. Always null-check before use.

## Exercise: primary-qualifier - @Primary and @Qualifier

### Goal
Resolve ambiguous beans without changing the dependent service.

### Task
1. Create two implementations: `BasicDocumentValidator` and `AiDocumentValidator`, both implementing `DocumentValidator`. Annotate both with `@Component`.
2. Mark `BasicDocumentValidator` with `@Primary`.
3. Create a second service `HighRiskKycService` that explicitly injects `AiDocumentValidator` using `@Qualifier("aiDocumentValidator")` in its constructor parameter.
4. Confirm `KycVerificationService` (from Exercise 1) still gets `BasicDocumentValidator` automatically.

### Gotcha
The `@Qualifier` value defaults to the bean name, which defaults to the uncapitalised class name. Rename the class and the qualifier breaks silently at startup.
