---
order: 10
---

# Starter Ecosystem

---

## Mental model

A Spring Boot starter is a dependency bundle.

Instead of adding Spring MVC, Tomcat, Jackson, validation, logging, and compatible versions yourself, you add one starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

That starter brings the needed libraries. Auto-configuration then wires them into Spring if conditions match.

Interview line:

```text
Starter = dependency bundle. Auto-configuration = bean creation logic.
```

---

## Common starters

| Starter | Brings |
| --- | --- |
| `spring-boot-starter-web` | Spring MVC, embedded Tomcat, Jackson |
| `spring-boot-starter-data-jpa` | Spring Data JPA, Hibernate, JDBC, transactions |
| `spring-boot-starter-security` | Spring Security framework |
| `spring-boot-starter-validation` | Bean Validation (`@NotNull`, `@Valid`) |
| `spring-boot-starter-test` | JUnit, Mockito, AssertJ, Spring Test |
| `spring-boot-starter-actuator` | Health, metrics, management endpoints |
| `spring-boot-starter-cache` | Spring Cache abstraction |
| `spring-boot-starter-aop` | Spring AOP support |

`spring-boot-starter` without a suffix is the base starter: Spring core, logging, and basic Boot support.

---

## Parent POM and BOM

Most Boot apps use `spring-boot-starter-parent` so dependency versions and common Maven plugin defaults are managed by Boot.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
```

Interview-level note: if a company cannot use Boot's parent POM, it can still import Boot's dependency BOM. You do not need to memorize the XML.

---

## Managed versions

Boot chooses compatible library versions. That is why you usually omit versions for starter-managed dependencies:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

Override versions only with a reason, usually a security fix or compatibility issue:

```xml
<properties>
    <jackson-bom.version>2.16.1</jackson-bom.version>
</properties>
```

Exact property names come from the Boot dependency BOM.

---

## Excluding transitive dependencies

Starters bring defaults. If you want a different embedded server, exclude the default and add the replacement.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

Do this when you have a real replacement. Do not exclude transitive dependencies blindly.

---

## Quick recall

**Q. What is a starter?**
A. A curated dependency bundle.

**Q. Starter vs auto-configuration?**
A. Starter brings libraries; auto-configuration creates beans from those libraries when conditions match.

**Q. Why can you omit dependency versions in Boot apps?**
A. Boot's parent/BOM manages compatible versions.

**Q. How do you swap Tomcat for Jetty?**
A. Exclude `spring-boot-starter-tomcat` from web starter and add `spring-boot-starter-jetty`.
