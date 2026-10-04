---
order: 20
search: false
---

# Solutions: Spring Boot Starter Ecosystem

---

## Solution: custom-starter-architecture - Custom Starter Module Decomposition & Naming

### 1. Rationale for Two-Module Separation
- **Separation of Concerns**: The `autoconfigure` module contains the actual Java code (auto-configuration classes, `@Conditional` beans, configuration properties). The `starter` module is an empty aggregator POM providing an opinionated set of dependencies.
- **Selective Consumption**: Some advanced consumers may want to supply custom auto-configurations or customize library versions without pulling the starter's full transitive dependency tree.
- **Build & Packaging Isolation**: Keeping auto-configuration logic in its own module allows cleaner testing with `@SpringBootTest` / `ApplicationContextRunner` without circular starter dependencies.

### 2. Naming Violation
- Official Spring Boot project starters reserve the `spring-boot-starter-*` prefix (e.g., `spring-boot-starter-web`).
- Third-party and custom internal starters must use the `*-spring-boot-starter` pattern (e.g., `acme-audit-spring-boot-starter`). Using `spring-boot-starter-*` violates Spring conventions and causes confusion about whether the starter is maintained by the Spring team.

### 3. Module Breakdown
- **`acme-audit-spring-boot-autoconfigure`**:
  - Contains Java configuration classes (`@AutoConfiguration`, `@Bean`, `@ConditionalOnClass`, `@ConditionalOnProperty`).
  - Contains configuration properties classes (`@ConfigurationProperties(prefix = "acme.audit")`).
  - Auto-configuration registration file: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (for Boot 3.x) or `META-INF/spring.factories` (Boot 2.x).
  - Depends on `spring-boot-autoconfigure` (and `spring-boot-configuration-processor` optional/annotation processor).
- **`acme-audit-spring-boot-starter`**:
  - `pom.xml` only (packaging `pom` or empty jar).
  - Dependencies: `acme-audit-spring-boot-autoconfigure`, core audit client SDK libraries, and any required third-party client drivers.

---

## Solution: bom-import-and-override - Multi-Module Dependency Management without Parent POM

### 1. Root POM Dependency Management Import
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

### 2. Overriding Managed Versions
Because Maven resolves `<dependencyManagement>` entries with **first-declaration-wins** priority across imported BOMs:
- When using `<scope>import</scope>`, declare the explicit `<dependency>` with your desired `<version>` **above** the `spring-boot-dependencies` BOM import in `<dependencyManagement>`:

```xml
<dependencyManagement>
    <dependencies>
        <!-- Explicit override placed BEFORE spring-boot-dependencies -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>
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
*(Note: Property overrides like `<mysql.version>` only work automatically if inheriting from `spring-boot-starter-parent` directly).*

### 3. Missing Parent POM Defaults & Plugin Configuration
- `spring-boot-starter-parent` configures default plugin executions, Java compiler target/source levels, and resource filtering.
- Without the parent POM, downstream executable service modules must explicitly configure the `repackage` goal for `spring-boot-maven-plugin`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## Solution: logging-framework-substitution - Replacing Logback with Log4j2

### 1. POM Exclusions & Replacement Starter
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-log4j2</artifactId>
    </dependency>
</dependencies>
```

### 2. Necessity of Starter Exclusions
- `spring-boot-starter-web` transitively pulls `spring-boot-starter`, which pulls `spring-boot-starter-logging` (containing `logback-classic`, `logback-core`, `jul-to-slf4j`, `log4j-to-slf4j`).
- Adding raw `log4j-core` without excluding Logback causes both Logback and Log4j2 bridges to compete on the classpath, leading to split logging routing or infinite redirection loops between `log4j-to-slf4j` and `log4j-slf4j-impl` / `log4j-slf4j2-impl`.
- Using `spring-boot-starter-log4j2` brings the complete, verified set of Log4j2 SLF4J binding adapters without manual jar coordination.

### 3. Failure Mode on Incomplete Exclusion
- **Multiple SLF4J Bindings / Provider Collision**: SLF4J emits `SLF4J: Class path contains multiple SLF4J providers` (or `Multiple SLF4J bindings` in SLF4J 1.x), arbitrarily picking Logback or Log4j2 depending on classpath order.
- **StackOverflowError Loop**: If `log4j-to-slf4j` and `log4j-slf4j2-impl` are both present simultaneously, calls to Log4j redirect to SLF4J, which redirects back to Log4j, producing an infinite recursive loop terminating in `java.lang.StackOverflowError` during startup.
