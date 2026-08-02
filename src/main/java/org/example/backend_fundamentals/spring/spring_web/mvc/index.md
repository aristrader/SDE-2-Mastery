---
order: 10
---

# Spring MVC

Row 7 — 🔴 💼 | D | 2 hrs

Spring MVC is the web layer used by Spring Boot to route HTTP requests to controller methods.

For interviews, focus on:

- What `DispatcherServlet` does
- How a request reaches a controller method
- When to use interceptor vs filter
- How request/response bodies are converted
- How `@RequestMapping` combines paths, HTTP methods, and media types

---

## DispatcherServlet

`DispatcherServlet` is Spring MVC's **Front Controller**.

It receives the HTTP request, finds the matching controller method, invokes it, and writes the response.

In Spring Boot, you usually do **not** declare it manually. Boot auto-registers it when `spring-boot-starter-web` is present.

Older Spring MVC apps could register it manually, but for normal Boot interview prep the important answer is:

> Boot creates and registers `DispatcherServlet`; our job is to write controllers.

---

## Request flow

Simplified request flow:

```text
HTTP request
    ↓
DispatcherServlet
    ↓
HandlerMapping finds the controller method
    ↓
HandlerAdapter invokes the method
    ↓
Argument resolvers fill parameters
    ↓
Controller method runs
    ↓
Return value is converted to response
```

Example:

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.findUser(id);
    }
}
```

For `GET /users/10`:

1. `DispatcherServlet` receives the request.
2. Spring matches it to `getUser`.
3. `@PathVariable` converts `"10"` into `Long id`.
4. The method returns `UserResponse`.
5. Jackson writes the object as JSON.

Know the flow and responsibilities.

---

## HandlerInterceptor

`HandlerInterceptor` lets you run logic before and after a controller method.

Common use cases:

- request logging
- correlation id / MDC setup
- audit timing
- simple pre-controller checks

```java
public class RequestTimingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        long start = (long) request.getAttribute("startTime");
        long tookMs = System.currentTimeMillis() - start;
        log.info("{} {} took {} ms", request.getMethod(), request.getRequestURI(), tookMs);
    }
}
```

Register it:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestTimingInterceptor())
            .addPathPatterns("/api/**");
    }
}
```

This registers the interceptor with Spring MVC, not with one controller class.

Scope it with path patterns:

- `addPathPatterns("/api/**")` applies to matching API routes.
- `excludePathPatterns("/api/public/**")` skips matching routes.
- If you do not restrict paths, it can apply broadly to MVC requests.

Important callback behavior:

- `preHandle` runs before the controller.
- Returning `false` stops the request; you must write the response yourself.
- `afterCompletion` runs after request completion and is useful for cleanup/logging.

---

## Interceptor vs Filter

| Use this | When |
|---|---|
| `Filter` | Work must happen before Spring MVC, such as security, CORS, compression, request wrapping |
| `HandlerInterceptor` | Work is Spring MVC-specific and may need controller/handler information |

Main difference:

- A `Filter` runs before `DispatcherServlet`.
- An `Interceptor` runs inside Spring MVC after a handler is chosen.

Use filters for low-level web concerns. Use interceptors for controller-level cross-cutting concerns.

---

## Argument resolvers

Argument resolvers are how Spring fills controller method parameters.

```java
@GetMapping("/users/{id}")
public UserResponse getUser(
        @PathVariable Long id,
        @RequestParam(defaultValue = "false") boolean includeOrders,
        @RequestHeader("X-Request-Id") String requestId) {
    return userService.findUser(id, includeOrders);
}
```

Spring resolves:

- `@PathVariable` from the URL path
- `@RequestParam` from the query string
- `@RequestHeader` from headers
- `@RequestBody` from the HTTP body

Spring uses this mechanism internally to populate controller parameters.

---

## Message converters

Message converters convert between HTTP bodies and Java objects.

They are used mainly for:

- `@RequestBody`: JSON request body → Java object
- `@ResponseBody` / `@RestController`: Java object → JSON response body

```java
@PostMapping("/users")
public UserResponse create(@RequestBody @Valid CreateUserRequest request) {
    return userService.create(request);
}
```

In a normal Spring Boot REST app:

- Jackson is used for JSON.
- Boot configures the JSON converter automatically.
- You usually customize Jackson through properties or an `ObjectMapper` customizer, not by touching converters directly.

Practical things to know:

- Wrong request `Content-Type` can cause `415 Unsupported Media Type`.
- Unsupported response `Accept` header can cause `406 Not Acceptable`.
- Invalid JSON can cause `400 Bad Request`.

---

## @RequestMapping

Use class-level mapping for a common base path and method-level mapping for individual operations.

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.findUser(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest request) {
        return userService.create(request);
    }
}
```

Final routes:

- `GET /api/v1/users/{id}`
- `POST /api/v1/users`

`consumes` means what request body type the endpoint accepts.

`produces` means what response body type the endpoint returns.

```java
@GetMapping(
    value = "/{id}",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public UserResponse getUser(@PathVariable Long id) {
    return userService.findUser(id);
}
```

For most REST APIs, you can rely on Boot defaults unless the API contract must be strict.

---

## Quick recall

**Q. What is `DispatcherServlet`?**  
A. Spring MVC's Front Controller. It receives requests, routes them to controller methods, and writes responses.

**Q. Do we manually declare `DispatcherServlet` in Spring Boot?**  
A. Usually no. Boot auto-registers it when the web starter is present.

**Q. What is the simplified MVC request flow?**  
A. Request → `DispatcherServlet` → handler mapping → controller invocation → message conversion/response.

**Q. Filter vs interceptor?**  
A. Filter runs before Spring MVC. Interceptor runs inside Spring MVC and can access handler/controller context.

**Q. What are message converters used for?**  
A. They convert request/response bodies, commonly JSON ↔ Java objects using Jackson.

**Q. What causes 415 vs 406?**  
A. 415 means request `Content-Type` is not supported. 406 means response type requested by `Accept` is not supported.

**Q. What does class-level `@RequestMapping` do?**  
A. It defines a base path shared by all handler methods in that controller.
