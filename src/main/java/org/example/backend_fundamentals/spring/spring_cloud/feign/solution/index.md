---
order: 20
search: false
---

# Feign / OpenFeign Solutions

## Solution: declare-feign-client - Declare the Feign client interface
```java
@FeignClient(name = "identity-service", url = "${identity.service.url}")
public interface IdentityServiceClient {
    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable("id") String id);

    @PostMapping("/users")
    UserResponse createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/users")
    UserResponse getUserByEmail(@RequestParam("email") String email);
}
```

## Solution: request-interceptor-auth - Request interceptor for auth propagation
```java
@Bean
public RequestInterceptor authInterceptor() {
    return template -> {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null && attrs.getRequest() != null) {
            String authHeader = attrs.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                template.header("Authorization", authHeader);
            }
        }
    };
}
```

## Solution: custom-errordecoder - Custom ErrorDecoder
```java
public class IdentityErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new UserNotFoundException("User not found");
            case 400 -> new ValidationException("Invalid request");
            case 503 -> new ServiceUnavailableException("Upstream identity service down");
            default  -> defaultDecoder.decode(methodKey, response);
        };
    }
}
```

## Solution: timeout-configuration - Timeout configuration
```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          identity-service:
            connectTimeout: 2000
            readTimeout: 5000
```
// Explanation: Because Feign is synchronous, an outbound call without a read timeout can block a Tomcat thread indefinitely. Under high load, this leads to thread starvation and cascading 503 errors across the whole API.

## Solution: wiremock-test-outline - WireMock test outline
```java
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "identity.service.url=http://localhost:${wiremock.server.port}"
})
class IdentityServiceClientTest {
    
    @Autowired
    IdentityServiceClient client;
    
    @Test
    void givenUserExists_whenGetUserById_thenReturnUser() {
        // Stub WireMock GET /users/123 to 200 JSON
        // Assert client.getUserById("123") matches JSON
    }
    
    @Test
    void givenUserMissing_whenGetUserById_thenThrowUserNotFoundException() {
        // Stub WireMock GET /users/999 to 404
        // AssertThrows UserNotFoundException on client.getUserById("999")
    }
}
```
