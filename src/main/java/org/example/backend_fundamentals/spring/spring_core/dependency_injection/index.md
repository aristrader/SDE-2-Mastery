---
order: 10
---

# Dependency Injection

---

## Core idea

Dependency Injection means a class declares the collaborators it needs, and Spring supplies them.

```java
@Service
public class OrderService {
    private final PaymentGateway paymentGateway;

    public OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
```

`OrderService` does not create `PaymentGateway`. That keeps object creation outside the business class and makes the dependency visible.

---

## Constructor injection

Use constructor injection for required dependencies.

```java
@Service
public class KycVerificationService {
    private final UserRepository userRepository;
    private final DocumentValidator documentValidator;

    public KycVerificationService(
            UserRepository userRepository,
            DocumentValidator documentValidator) {
        this.userRepository = userRepository;
        this.documentValidator = documentValidator;
    }
}
```

Why interviewers prefer this:

- Required dependencies are visible in one place.
- Fields can be `final`.
- The object is fully initialized after construction.
- Unit tests can use plain Java: `new KycVerificationService(repo, validator)`.
- A long constructor exposes a design smell: the class may be doing too much.

Since Spring 4.3, `@Autowired` is optional when the class has exactly one constructor.

With Lombok, the common production style is:

```java
@Service
@RequiredArgsConstructor
public class KycVerificationService {
    private final UserRepository userRepository;
    private final DocumentValidator documentValidator;
}
```

Gotcha: `@RequiredArgsConstructor` only includes `final` fields and `@NonNull` fields. If a dependency is not `final`, Lombok will not put it in the constructor.

---

## Field injection

Field injection works in a running Spring app, but avoid it in application code.

```java
@Service
public class OrderService {
    @Autowired
    private PaymentGateway paymentGateway;
}
```

Problems:

- Dependencies are hidden across fields.
- Fields cannot be `final`.
- Plain unit tests cannot construct the object correctly without Spring or reflection.
- A class with many dependencies does not look painful until runtime or refactoring.

Use this only in legacy code or tiny examples where testability is not the point.

---

## Setter injection

Use setter injection only for optional dependencies or rare lifecycle/circular-dependency workarounds.

```java
@Service
public class ReportService {
    private NotificationService notificationService;

    @Autowired(required = false)
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

If the bean is missing, Spring skips the setter. The field remains `null`, so the code must handle that.

For optional dependencies, `ObjectProvider<T>` is usually cleaner because it still works with constructor injection.

---

## Multiple beans of same type

If two beans implement the same interface, Spring needs help choosing one.

```java
public interface PaymentGateway {
    void charge(Order order);
}

@Component("stripeGateway")
public class StripeGateway implements PaymentGateway { ... }

@Component("razorpayGateway")
public class RazorpayGateway implements PaymentGateway { ... }
```

This injection is ambiguous:

```java
@Service
public class CheckoutService {
    private final PaymentGateway paymentGateway;

    public CheckoutService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
```

Spring sees two possible `PaymentGateway` beans and cannot know whether checkout should use Stripe or Razorpay.

Use `@Qualifier` when the injection point needs a specific bean:

```java
@Service
public class CheckoutService {
    private final PaymentGateway paymentGateway;

    public CheckoutService(@Qualifier("stripeGateway") PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
```

Use `@Primary` when one implementation should be the default:

```java
@Primary
@Component
public class StripeGateway implements PaymentGateway { ... }
```

Then this works because `StripeGateway` is the default:

```java
public CheckoutService(PaymentGateway paymentGateway) {
    this.paymentGateway = paymentGateway;
}
```

You do not need `@Qualifier` or `@Primary` when there is only one bean of that type:

```java
@Component
public class StripeGateway implements PaymentGateway { ... }

@Service
public class CheckoutService {
    public CheckoutService(PaymentGateway paymentGateway) {
        // only one PaymentGateway exists, so Spring injects StripeGateway
    }
}
```

You also usually do not put `@Qualifier` on the main service when the choice is runtime data. Example: if each order says which gateway to use, checkout should not hard-code Stripe in its constructor. Use a resolver instead.

Interview line: Spring first resolves by type. If more than one candidate exists, `@Qualifier` is explicit; `@Primary` is the default fallback. If ambiguity remains, Spring may try matching the parameter/field name; if it still cannot choose exactly one, startup fails.

---

## Injecting all implementations

When runtime selection is needed, inject a collection or map.

```java
@Service
public class PaymentGatewayResolver {
    private final Map<String, PaymentGateway> gateways;

    public PaymentGatewayResolver(Map<String, PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway forName(String name) {
        PaymentGateway gateway = gateways.get(name);
        if (gateway == null) {
            throw new IllegalArgumentException("Unsupported gateway: " + name);
        }
        return gateway;
    }
}
```

Spring injects all `PaymentGateway` beans into the map, keyed by bean name:

```text
stripeGateway -> StripeGateway
razorpayGateway -> RazorpayGateway
```

This is useful for Strategy/registry-style code.

Usage:

```java
@Service
public class CheckoutService {
    private final PaymentGatewayResolver gatewayResolver;

    public CheckoutService(PaymentGatewayResolver gatewayResolver) {
        this.gatewayResolver = gatewayResolver;
    }

    public void checkout(Order order) {
        PaymentGateway gateway = gatewayResolver.forName(order.paymentGatewayName());
        gateway.charge(order);
    }
}
```

Here `CheckoutService` does not need `@Qualifier`, because it does not want one fixed implementation. It asks the resolver at runtime.

Rule of thumb:

| Situation | Use |
| --- | --- |
| Only one implementation exists | Plain constructor injection |
| One implementation should be the app-wide default | `@Primary` |
| This class always needs one specific implementation | `@Qualifier` |
| Request/order/user data decides implementation at runtime | Resolver with `Map<String, T>` |

---

## ObjectProvider

Use `ObjectProvider<T>` when a dependency is optional, lazy, or should be requested fresh each time.

```java
@Service
public class ReportService {
    private final ObjectProvider<AuditLogger> auditLogger;

    public ReportService(ObjectProvider<AuditLogger> auditLogger) {
        this.auditLogger = auditLogger;
    }

    public void generate() {
        auditLogger.ifAvailable(logger -> logger.log("report generated"));
    }
}
```

Common methods:

| Method | Meaning |
| --- | --- |
| `getObject()` | Resolve now; fail if missing |
| `getIfAvailable()` | Return bean or `null` |
| `ifAvailable(...)` | Run only if bean exists |

Use case: optional integrations, expensive/lazy collaborators, or prototype beans needed from a singleton.

---

## Circular dependencies

Constructor cycles fail at startup:

```text
ServiceA -> ServiceB -> ServiceA
```

That is usually good. It exposes a design problem early.

Bad quick fixes:

- `@Lazy` on one constructor parameter
- setter injection on one side

They can unblock wiring, but they do not fix the design. The better fix is usually to extract the shared responsibility into a third service or publish an event instead of calling back directly.

Example temporary workaround:

```java
@Service
public class OrderService {
    private final PaymentService paymentService;

    public OrderService(@Lazy PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

`@Lazy` makes Spring inject a proxy first and resolve the real `PaymentService` later. This is okay only as a temporary legacy workaround when refactoring immediately is risky and the cycle is understood. It should not be the final design.

When it is acceptable:

- Legacy code where a direct refactor would be risky right now.
- A short-lived migration step while extracting a third service or event.
- Framework integration code where lifecycle ordering is genuinely constrained.

In normal application design, circular dependencies are not okay. They usually mean two services own mixed responsibilities.

---

## Quick recall

**Q. Which injection style should you prefer?**
A. Constructor injection.

**Q. Why is field injection discouraged?**
A. Hidden dependencies, no `final`, harder plain unit tests.

**Q. Lombok `@RequiredArgsConstructor` gotcha?**
A. It only includes `final` and `@NonNull` fields. Non-final dependencies are skipped.

**Q. When is setter injection acceptable?**
A. Optional dependency, or rare temporary workaround for a circular dependency.

**Q. What if two beans implement the same interface?**
A. Use `@Qualifier` for explicit choice or `@Primary` for default choice.

**Q. How do you inject all implementations of an interface?**
A. Use `List<T>` or `Map<String, T>`; map keys are bean names.

**Q. When is `@Qualifier` not the right tool?**
A. When the implementation is chosen at runtime; inject a resolver backed by `Map<String, T>` instead.

**Q. What is `ObjectProvider` for?**
A. Lazy, optional, or fresh-per-call dependency lookup without injecting `ApplicationContext`.

**Q. What does a constructor circular dependency usually mean?**
A. The design is coupled incorrectly; extract a third responsibility or use events.

**Q. When is a circular dependency workaround acceptable?**
A. Temporarily in legacy/migration code, with a plan to remove it. It should not be the final design.
