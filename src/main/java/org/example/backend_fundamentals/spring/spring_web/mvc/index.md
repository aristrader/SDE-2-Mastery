---
order: 30
---

# Spring MVC

Row 7 — 🔴 💼 | D | 2 hrs

---

## DispatcherServlet — the Front Controller

`DispatcherServlet` is a single `jakarta.servlet.http.HttpServlet` at the entry point of every HTTP request. (Boot 2.x used `javax.servlet`; Boot 3.x migrated to `jakarta.servlet` as part of the Jakarta EE 9+ baseline.) Instead of one servlet per URL (the old model), one servlet handles everything and delegates to the right handler internally.

**Registration:**
- Classic: declared in `web.xml` with `<servlet-mapping>` to `/*` or `/`
- Programmatic: implement `WebApplicationInitializer`, call `context.addServlet(...).addMapping("/")`
- Spring Boot: `DispatcherServletAutoConfiguration` registers it automatically via `DispatcherServletRegistrationBean`; you never touch `web.xml`

**ApplicationContext hierarchy:**
- Root `WebApplicationContext` — created by `ContextLoaderListener`; holds service/repo beans shared across servlets
- Servlet `WebApplicationContext` — child of root; holds MVC beans (controllers, `HandlerMapping`, `ViewResolver`) scoped to this `DispatcherServlet`
- Spring Boot collapses these into a single context by default

---

## Request processing pipeline

```
Client HTTP Request
      │
      ▼
DispatcherServlet.doDispatch()
      │
      ├─► HandlerMapping.getHandler()
      │       → returns HandlerExecutionChain (handler + interceptors)
      │
      ├─► HandlerAdapter.supports(handler)
      │       → picks the adapter that knows how to invoke this handler
      │
      ├─► HandlerInterceptor.preHandle()   ← runs before controller
      │
      ├─► HandlerAdapter.handle()
      │       → resolves method arguments (via HandlerMethodArgumentResolvers)
      │       → invokes controller method
      │       → converts return value (via HandlerMethodReturnValueHandlers)
      │       → returns ModelAndView (null if @ResponseBody)
      │
      ├─► HandlerInterceptor.postHandle()  ← after controller, before view
      │
      ├─► ViewResolver.resolveViewName()  ← skipped for @ResponseBody
      │       → maps logical name (e.g. "home") to a View object
      │
      ├─► View.render()
      │
      └─► HandlerInterceptor.afterCompletion()  ← always, even on exception
```

**Key class for @RequestMapping:** `RequestMappingHandlerMapping` scans `@Controller` beans and builds a map of `(method, path, consumes, produces, headers, params)` → handler method at startup.

**Key adapter:** `RequestMappingHandlerAdapter` knows how to invoke `HandlerMethod` objects — it wires together argument resolvers, return value handlers, and message converters.

---

## HandlerInterceptors

Implement `HandlerInterceptor`:

```java
public interface HandlerInterceptor {
    // return false to abort the chain (e.g. auth failed — write 401 yourself)
    default boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) { return true; }

    // only called if handler returned normally (not on exception)
    default void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView mav) {}

    // always called after response committed (cleanup — close resources, log timing)
    default void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {}
}
```

Register via `WebMvcConfigurer.addInterceptors()`. Order is insertion order; `preHandle` fires in order, `postHandle` and `afterCompletion` fire in reverse.

**Common uses:** authentication checks in `preHandle`, MDC population (request-id), per-request timing, audit logging in `afterCompletion`.

**Interceptor vs Filter:**
| | `HandlerInterceptor` | `jakarta.servlet.Filter` |
|---|---|---|
| Runs relative to DispatcherServlet | After DS — inside `doDispatch()` | Before DispatcherServlet |
| Knows the handler | Yes — receives `handler` param | No |
| Access to `ModelAndView` | Yes (postHandle) | No |
| Applied to | Only requests dispatched through `DispatcherServlet` | All requests including static assets, error pages |

A `Filter` runs in the servlet container before `DispatcherServlet` sees the request — no access to handler or `ModelAndView`, but intercepts every request regardless of whether Spring MVC handles it. Use `Filter` for things that must run even if Spring MVC isn't involved (e.g., raw CORS headers, request body logging before routing).

---

## HandlerMethodArgumentResolver

How `@RequestBody`, `@PathVariable`, `@RequestParam`, etc. get populated — each annotation has a dedicated resolver.

```java
public interface HandlerMethodArgumentResolver {
    boolean supportsParameter(MethodParameter parameter);
    Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                           NativeWebRequest webRequest, WebDataBinderFactory binderFactory);
}
```

`RequestMappingHandlerAdapter` iterates its ordered list of resolvers; first one where `supportsParameter()` returns `true` wins.

**Built-in resolvers (sample):**

| Resolver | Handles |
|---|---|
| `RequestResponseBodyMethodProcessor` | `@RequestBody` + `@ResponseBody` |
| `PathVariableMethodArgumentResolver` | `@PathVariable` |
| `RequestParamMethodArgumentResolver` | `@RequestParam` + simple scalar params |
| `RequestHeaderMethodArgumentResolver` | `@RequestHeader` |
| `SessionAttributeMethodArgumentResolver` | `@SessionAttribute` |

**Custom resolver pattern:**

```java
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter param, ModelAndViewContainer mav,
                                   NativeWebRequest req, WebDataBinderFactory binder) {
        // pull authenticated user from SecurityContext or JWT claim
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

// register
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}
```

---

## MessageConverters — @RequestBody / @ResponseBody serialization

`HttpMessageConverter<T>` reads the HTTP body into a Java type (`@RequestBody`) and writes a Java type back to the body (`@ResponseBody`).

**Selection:** `RequestResponseBodyMethodProcessor` iterates the converter list and picks the first that:
1. `canRead(targetType, contentType)` — for `@RequestBody`
2. `canWrite(returnType, acceptedMediaTypes)` — for `@ResponseBody`

**Default converters registered by Spring MVC (order matters):**

| Converter | Handles |
|---|---|
| `ByteArrayHttpMessageConverter` | `byte[]` ↔ `application/octet-stream` |
| `StringHttpMessageConverter` | `String` ↔ `text/plain`, `*/*` |
| `ResourceHttpMessageConverter` | `Resource` ↔ `application/octet-stream` |
| `MappingJackson2HttpMessageConverter` | Any POJO ↔ `application/json` |
| `Jaxb2RootElementHttpMessageConverter` | JAXB-annotated ↔ `application/xml` |

Spring Boot auto-configures Jackson via `JacksonAutoConfiguration`. Customize it:

```java
@Bean
public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder -> builder
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
}
```

**Content negotiation:** `ContentNegotiationManager` determines the response media type:
1. Path extension (`.json`) — deprecated, disabled by default
2. `format` query param — disabled by default
3. `Accept` header — primary mechanism
4. Default media type — configured fallback

---

## @RequestMapping — class vs method level

```java
@RestController
@RequestMapping("/api/v1/users")          // class-level: base path for all methods
public class UserController {

    @GetMapping("/{id}")                  // resolves to GET /api/v1/users/{id}
    public User getUser(@PathVariable Long id) { ... }

    @PostMapping                          // resolves to POST /api/v1/users
    @RequestMapping(
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,   // only accepts JSON body
        produces = MediaType.APPLICATION_JSON_VALUE    // only produces JSON
    )
    public User createUser(@RequestBody @Valid CreateUserRequest req) { ... }
}
```

`produces` triggers 406 Not Acceptable if the client's `Accept` header doesn't match. `consumes` triggers 415 Unsupported Media Type if `Content-Type` doesn't match. Useful for strict API contracts.

---

## Quick recall

**Q. What is DispatcherServlet and why is there only one?**
A. It's the Front Controller — single entry point for all HTTP; delegates internally to handlers. One servlet avoids per-URL servlet sprawl and centralizes cross-cutting concerns.

**Q. In what order do HandlerInterceptor callbacks fire?**
A. `preHandle` (request order) → controller → `postHandle` (reverse order) → view → `afterCompletion` (reverse order, always).

**Q. Why return `false` from `preHandle`?**
A. To short-circuit the chain — the handler and subsequent interceptors won't run. You must write the response yourself (e.g. send 401) before returning false.

**Q. How does Spring know which converter to use for `@ResponseBody`?**
A. Content negotiation picks the target media type from `Accept` header, then the converter list is scanned for the first `canWrite(type, mediaType)` match — typically `MappingJackson2HttpMessageConverter` for JSON.

**Q. HandlerInterceptor vs Filter — when to use which?**
A. Filter for pre-Spring concerns (raw CORS, body buffering, applies to static assets too). Interceptor when you need handler/ModelAndView access or want to scope to MVC-dispatched requests only.

**Q. How do you inject a custom object into a controller method parameter?**
A. Implement `HandlerMethodArgumentResolver`, register via `WebMvcConfigurer.addArgumentResolvers()`. `supportsParameter()` selects it; `resolveArgument()` builds the value.

**Q. What triggers a 415 Unsupported Media Type vs a 406 Not Acceptable?**
A. 415: request `Content-Type` doesn't match controller's `consumes`. 406: client's `Accept` header doesn't match controller's `produces`.


