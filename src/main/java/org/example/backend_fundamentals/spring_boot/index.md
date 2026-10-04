---
order: 30
---
# Spring Boot

Overview of spring boot.

Spring Boot is an opinionated layer built on top of the Spring framework. It eliminates boilerplate configuration through starter dependency bundles, automatic configuration of sensible defaults, embedded servlet containers, and production-ready operational endpoints.

## Sibling relationship with Spring

While the `spring` section covers core framework mechanics (IoC, MVC, Data, Security, Cloud), `spring_boot` focuses on runtime bootstrapping, dependency auto-wiring, and production monitoring. A thorough backend preparation combines both: Spring for the underlying component mechanics and Spring Boot for real-world application assembly.

## Prerequisites

- Familiarity with Spring Core concepts: beans, dependency injection, and configuration classes.
- Basic understanding of Maven or Gradle build files and transitive dependency resolution.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `starters` | Curated dependency descriptors (`spring-boot-starter-*`), transitive version curation, and the `spring-boot-dependencies` BOM. |
| 2 | `auto_configuration` | Opinionated default wiring via `@EnableAutoConfiguration`, condition evaluation (`@ConditionalOnClass`, `@ConditionalOnMissingBean`), and configuration ordering. |
| 3 | `actuator` | Production observability endpoints (`/actuator/health`, `/actuator/metrics`), health indicator contracts, and security controls. |

## Next action

Start with `starters` and `auto_configuration` to understand how Spring Boot builds and initializes an application before exploring operational endpoints in `actuator`.

## Quick recall

**Q. What three annotations are combined into `@SpringBootApplication`?**
A. `@SpringBootConfiguration` (specialized `@Configuration`), `@EnableAutoConfiguration` (triggers auto-configuration imports), and `@ComponentScan` (scans current package and subpackages).

**Q. How does `@ConditionalOnMissingBean` support developer customization?**
A. It tells Spring Boot to create its default auto-configured bean only if the application has not already registered its own bean definition of that type, ensuring user configuration takes precedence.

**Q. What is the primary purpose of Spring Boot starters?**
A. To provide curated, pre-tested dependency bundles with managed compatible versions, avoiding manual dependency and version mismatch management in build scripts.

**Q. How do Spring Boot Actuator health checks report system status?**
A. By aggregating individual `HealthIndicator` beans into a consolidated status (`UP`, `DOWN`, `OUT_OF_SERVICE`) exposed via the `/actuator/health` endpoint.
