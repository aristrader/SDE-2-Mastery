---
order: 10
search: false
---

# Exercises: Spring Boot Starter Ecosystem

---

## Exercise: custom-starter-architecture - Custom Starter Module Decomposition & Naming

Your team is tasked with building an internal security audit library used across 30+ Spring Boot microservices in your organization.

1. Why does the official Spring Boot starter specification recommend splitting custom starters into two separate modules (`acme-audit-spring-boot-autoconfigure` and `acme-audit-spring-boot-starter`) rather than combining all code into a single artifact?
2. What naming violation occurs if the artifact is published as `spring-boot-starter-acme-audit`?
3. Detail what components, metadata, and POM dependencies belong in the `autoconfigure` module versus the `starter` module.

---

## Exercise: bom-import-and-override - Multi-Module Dependency Management without Parent POM

You are migrating a legacy multi-module Maven application to Spring Boot. The root POM already inherits from an organization-wide corporate parent (`com.acme:corporate-parent-pom:4.1.0`) and cannot be changed to `spring-boot-starter-parent`.

1. Write the Maven XML snippet required to import Spring Boot's dependency management into your root POM.
2. If you need to upgrade a specific transitive dependency version managed by Spring Boot (e.g., `mysql:mysql-connector-j` or Jackson) due to a critical CVE patch, how must you place that property or dependency declaration relative to the imported BOM?
3. What default behavior provided by `spring-boot-starter-parent` is missing when using only BOM import, and how do you configure the `spring-boot-maven-plugin` in downstream service modules?

---

## Exercise: logging-framework-substitution - Replacing Logback with Log4j2

A corporate compliance mandate requires transitioning a Spring Boot REST service (`spring-boot-starter-web`) from default Logback to Log4j2 for asynchronous audit logging.

1. Write the Maven dependency configuration to exclude default logging and attach `spring-boot-starter-log4j2`.
2. Why is excluding `spring-boot-starter-logging` or excluding Logback from `spring-boot-starter` necessary rather than merely adding `log4j-core`?
3. What runtime failure or warning occurs if transitive logging dependencies are not cleanly excluded before adding Log4j2?
