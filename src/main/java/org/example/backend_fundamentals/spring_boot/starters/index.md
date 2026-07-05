---
order: 30
---

# Starter Ecosystem

---

## What a starter actually is

A Spring Boot starter is a **convenience POM** — it bundles three things in one dependency declaration:

1. **Dependency set** — the libraries you need (e.g., Tomcat, Spring MVC, Jackson), pinned to compatible versions
2. **Auto-configuration** — the `@Configuration` classes that wire those libraries into the Spring context
3. **Transitive resolution** — you declare one artifact; Maven/Gradle pulls everything else

Without starters you'd declare 5-8 dependencies per integration, pin compatible versions, and write your own `@Bean` definitions. Starters collapse that to one line.

```xml
<!-- One starter, dozens of dependencies resolved transitively -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

---

## `spring-boot-starter-parent`

The parent POM all Spring Boot apps inherit from:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
```

What it provides:
- **`<dependencyManagement>`** — a curated BOM (Bill of Materials) with compatible versions for ~300 libraries. Child POMs inherit these versions without specifying them.
- **Plugin configuration** — `spring-boot-maven-plugin` pre-configured, compiler plugin set to the right Java version, resource filtering enabled.
- **Default encoding** — UTF-8 for source and resources.
- **Java version** — `java.version` property pre-set (you override it).

`spring-boot-starter-parent` extends `spring-boot-dependencies`, the pure BOM. If you can't use `spring-boot-starter-parent` as your parent (common in enterprise multi-module projects with a corporate parent POM), import `spring-boot-dependencies` as a BOM instead:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.2.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## Key starters and what they pull in

| Starter | What it brings |
|---|---|
| `spring-boot-starter-web` | Embedded Tomcat, Spring MVC, Jackson (JSON), validation-api |
| `spring-boot-starter-webflux` | Netty (reactive server), Spring WebFlux, Project Reactor |
| `spring-boot-starter-data-jpa` | Hibernate ORM, Spring Data JPA, JDBC, transaction management |
| `spring-boot-starter-data-redis` | Lettuce client (default), Spring Data Redis |
| `spring-boot-starter-security` | Spring Security (auth + authz framework, no rules by default) |
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ, Hamcrest, Spring Test, `@SpringBootTest` |
| `spring-boot-starter-actuator` | Micrometer, production management endpoints |
| `spring-boot-starter-validation` | Hibernate Validator, Bean Validation API (`@NotNull`, `@Valid`, etc.) |
| `spring-boot-starter-cache` | Spring Cache abstraction (`@Cacheable`, `@CacheEvict`, `@CachePut`) |
| `spring-boot-starter-aop` | AspectJ weaving + Spring AOP support |

`spring-boot-starter` (no suffix) is the bare minimum: Spring core, logging (Logback + SLF4J), and YAML support. All other starters pull it in transitively.

---

## Overriding a managed version

The parent BOM pins all versions. Override in your POM's `<properties>` block:

```xml
<properties>
    <java.version>17</java.version>
    <jackson.version>2.16.1</jackson.version>     <!-- overrides Boot's managed Jackson version -->
    <hibernate.version>6.4.0.Final</hibernate.version>
</properties>
```

Property names are documented in the `spring-boot-dependencies` BOM — look up the exact name there, it's not always obvious (e.g., `jackson-bom.version` vs `jackson.version` depending on Boot version).

---

## Excluding a transitive dependency

Starters bundle opinionated defaults. Common exclusion: swap out Tomcat for Undertow, or remove the default Jackson in favor of Gson.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <!-- Swap embedded server: remove Tomcat, add Undertow -->
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

---

## @EnableAutoConfiguration trigger chain

`@SpringBootApplication` is a composed annotation that includes `@EnableAutoConfiguration`. What fires at startup:

1. `@EnableAutoConfiguration` imports `AutoConfigurationImportSelector`
2. `AutoConfigurationImportSelector` reads `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Boot 3.x) — or `META-INF/spring.factories` key `EnableAutoConfiguration` (Boot 2.x) — from every jar on the classpath
3. Each listed class is a `@Configuration` annotated with `@AutoConfiguration` (or `@Configuration` in older starters)
4. `@Conditional*` annotations on each auto-config class are evaluated — only those whose conditions pass are applied
5. `@Import`, `@Bean`, and `@EnableConfigurationProperties` inside those classes register beans into the context

Result: beans for Tomcat, Jackson, DataSource, etc. without any explicit `@Bean` definition — purely because the starter jar is present and the conditions are met.

---

## Creating a custom starter

The canonical Spring Boot pattern is **two modules** (the library code is a separate concern — the starter itself is autoconfigure + thin wrapper):

```
my-feature/
├── my-feature-autoconfigure/    # @Configuration + @Conditional wiring
└── my-feature-spring-boot-starter/  # thin POM, depends on autoconfigure (+ library if needed)
```

Many teams add a third library module when the core logic should be usable without Spring Boot (e.g., an SDK) — valid, but not required by the starter pattern.

### Module 1: `my-feature-autoconfigure`

```java
@Configuration
@ConditionalOnClass(MyFeatureClient.class)              // only if library is on classpath
@ConditionalOnProperty(prefix = "myfeature", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MyFeatureProperties.class)
public class MyFeatureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean                           // user can override by defining their own
    public MyFeatureClient myFeatureClient(MyFeatureProperties props) {
        return new MyFeatureClient(props.getApiKey(), props.getBaseUrl());
    }
}
```

Register in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Boot 3.x):

```
com.mycompany.myfeature.MyFeatureAutoConfiguration
```

### Module 2: `my-feature-spring-boot-starter`

An almost empty POM:

```xml
<dependencies>
    <dependency>
        <groupId>com.mycompany</groupId>
        <artifactId>my-feature-autoconfigure</artifactId>
    </dependency>
    <!-- add my-feature-library here too if it is a separate module -->
</dependencies>
```

Users add only the starter — auto-configuration wires up automatically.

**Why the split?** Users wanting manual control can import `my-feature-autoconfigure` directly and skip the starter. The autoconfigure module is optional-dependency-gated, so it has zero impact if the user defines their own beans.

---

## Naming convention

Spring Boot's own starters follow `spring-boot-starter-{name}`. Third-party starters should follow `{name}-spring-boot-starter` (reversed) to avoid confusion with official starters. Example: `mybatis-spring-boot-starter`, not `spring-boot-starter-mybatis`.

---

## Interview gotchas

**"What's the difference between `spring-boot-starter-parent` and `spring-boot-dependencies`?"**
`spring-boot-starter-parent` extends `spring-boot-dependencies` and adds plugin config + encoding defaults. Use `spring-boot-dependencies` as a BOM import when you can't inherit from the parent (corporate POM conflict).

**"Does adding a starter automatically configure everything?"**
The starter brings in the auto-config, but the auto-config's `@Conditional` conditions must pass. Adding `spring-boot-starter-data-jpa` without configuring a `spring.datasource.url` will fail at startup because `DataSourceAutoConfiguration` requires a URL.

**"What are the two required modules in a custom starter?"**
`autoconfigure` module (holds `@Configuration` + `@Conditional` beans, registered in `AutoConfiguration.imports`) and the thin `starter` POM that depends on it. A third library module is a common addition when the core code must be usable without Spring Boot, but isn't part of the starter pattern.

**"How does `@EnableAutoConfiguration` actually work?"**
It imports `AutoConfigurationImportSelector`, which reads `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` from every jar on the classpath, then evaluates `@Conditional` guards on each listed config class. Only conditions that pass result in beans being registered.

**"How do you swap Tomcat for Jetty?"**
Exclude `spring-boot-starter-tomcat` from `spring-boot-starter-web`, add `spring-boot-starter-jetty`. Spring Boot's `EmbeddedWebServerFactoryCustomizerAutoConfiguration` picks up whichever embedded server is on the classpath.

---

## Quick recall

**Q. What does a Spring Boot starter provide?**
A. A curated dependency set + compatible versions + auto-configuration that wires those dependencies — one artifact instead of many.

**Q. What is `spring-boot-starter-parent` for?**
A. Provides `<dependencyManagement>` with compatible library versions, plugin config, encoding, and Java version defaults — child POMs inherit without specifying individual versions.

**Q. How do you use Boot's managed versions without inheriting from `spring-boot-starter-parent`?**
A. Import `spring-boot-dependencies` as a BOM in `<dependencyManagement>` with `<type>pom</type><scope>import</scope>`.

**Q. How do you override a managed dependency version?**
A. Set the property in `<properties>` (e.g., `<jackson.version>2.16.1</jackson.version>`) — overrides the version defined in the parent BOM.

**Q. What are the two required modules in a custom starter?**
A. `autoconfigure` module (`@Configuration` + `@Conditional`, registered in `AutoConfiguration.imports`) and the thin `starter` POM that depends on it. A third library module is optional.

**Q. How does `@EnableAutoConfiguration` trigger auto-configuration?**
A. It imports `AutoConfigurationImportSelector`, which reads every jar's `AutoConfiguration.imports` file and applies only the configs whose `@Conditional` guards pass.

**Q. Naming convention for third-party starters?**
A. `{name}-spring-boot-starter` (e.g., `mybatis-spring-boot-starter`), not `spring-boot-starter-{name}` — reserved for official starters.


<ExerciseNav />
