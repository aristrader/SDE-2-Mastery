---
order: 20
---

# Spring Web

Spring Web provides the HTTP request-processing infrastructure for enterprise Java applications, spanning traditional Servlet-based Spring MVC, RESTful API design, JSON serialization, global exception handling, and reactive event-loop execution via WebFlux.

## Prerequisites

- HTTP protocol fundamentals: request/response anatomy, headers, status codes, and HTTP verbs.
- Java Servlet specification basics: `HttpServlet`, filters, request dispatching, and thread-per-request execution.
- JSON structure and Java serialization/deserialization basics.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `mvc` | Front controller architecture, `DispatcherServlet` pipeline, `HandlerMapping`, `HandlerAdapter`, and interceptors. |
| 2 | `rest` | RESTful API development, `@RestController`, request binding (`@RequestBody`, `@PathVariable`), and input validation. |
| 3 | `jackson` | JSON serialization tuning, `@JsonProperty`, `@JsonIgnore`, `@JsonView`, Java 8 date/time modules, and custom serializers. |
| 4 | `exception_handling` | Centralized error handling with `@ExceptionHandler`, `@ControllerAdvice`, and standardized RFC 7807 `ProblemDetail` responses. |
| 5 | `webflux_threading_model` | Non-blocking reactive execution, Project Reactor (`Mono`/`Flux`), Netty event-loop threading vs Tomcat worker pools. |
| 6 | `api_quick_revision` | Rapid interview reference for common Spring Web annotations, status codes, and controller patterns. |

## Next action

Start with `mvc` to understand how `DispatcherServlet` orchestrates requests from HTTP entry to handler return, then proceed to `rest` and `exception_handling` for modern API construction.

## Quick recall

**Q. What is the role of `DispatcherServlet` in Spring MVC?**
A. It acts as the front controller, intercepting all incoming HTTP requests and coordinating handler mapping, execution chain invocation, argument resolution, and response serialization.

**Q. How does `@RestController` differ from `@Controller`?**
A. `@RestController` combines `@Controller` and `@ResponseBody`, instructing Spring to serialize method return values directly into the HTTP response body (typically via Jackson) rather than resolving views.

**Q. How does the WebFlux threading model differ from standard Spring MVC?**
A. Spring MVC uses a thread-per-request model backed by a servlet worker pool (e.g., Tomcat), whereas WebFlux uses an asynchronous non-blocking event-loop model (Netty) with a small fixed number of event-loop threads.

**Q. How should exceptions be handled consistently across a Spring REST API?**
A. By defining a `@RestControllerAdvice` class containing `@ExceptionHandler` methods that translate caught domain/framework exceptions into consistent `ResponseEntity` or `ProblemDetail` payloads.
