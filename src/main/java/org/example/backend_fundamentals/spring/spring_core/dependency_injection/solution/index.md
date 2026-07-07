---
order: 20
search: false
---

# Dependency Injection Solutions

## Solution: wire-requiredargsconstructor - Wire a service with @RequiredArgsConstructor
```java
@Service
@RequiredArgsConstructor
public class KycVerificationService {
    private final UserRepository userRepository;
    private final DocumentValidator documentValidator;

    public boolean verify(String userId, String docType, byte[] content) {
        return userRepository.findById(userId).isPresent() && documentValidator.validate(docType, content);
    }
}
```

## Solution: unit-test-no-spring - Unit test without Spring
```java
public static void main(String[] args) {
    UserRepository stubRepo = id -> Optional.of(new User());
    DocumentValidator stubValidator = (doc, bytes) -> true;
    
    KycVerificationService service = new KycVerificationService(stubRepo, stubValidator);
    System.out.println(service.verify("U1", "PASSPORT", new byte[0])); // true
}
```

## Solution: compare-field-injection - Compare field injection
```java
// Cost 1: fields cannot be final, mutating state is possible.
// Cost 2: unit test requires Spring context or reflection (e.g. Mockito @InjectMocks).
// Cost 3: missing dependencies fail at runtime instead of compile time.
```

## Solution: optional-dependency-setter - Optional dependency via setter injection
```java
private NotificationService notificationService;

@Autowired(required = false)
public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
}

// In verify():
if (notificationService != null) {
    notificationService.sendAlert(userId, "Verified");
}
```

## Solution: primary-qualifier - @Primary and @Qualifier
```java
@Component
@Primary
public class BasicDocumentValidator implements DocumentValidator { ... }

@Component("aiValidator")
public class AiDocumentValidator implements DocumentValidator { ... }

@Service
public class HighRiskKycService {
    private final DocumentValidator validator;
    public HighRiskKycService(@Qualifier("aiValidator") DocumentValidator validator) {
        this.validator = validator;
    }
}
```
