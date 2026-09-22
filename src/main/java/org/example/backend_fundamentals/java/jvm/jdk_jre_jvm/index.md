---
order: 10
---

# JDK, JRE, and JVM

The names describe responsibilities, not a guarantee about how every vendor packages a download.

| Term | Interview-safe meaning | What it is not |
| --- | --- | --- |
| JVM | The virtual machine specification and its implementation that loads and executes class-file bytecode. | A compiler for `.java` source. |
| Runtime | A JVM plus the Java platform libraries required by an application. | A development toolchain. |
| JRE | The traditional name for that runtime bundle. | A separate download that every modern vendor must ship. |
| JDK | A development distribution: runtime plus tools such as `javac`, `javadoc`, `jar`, and diagnostic tools. | Just the compiler. |

Modern deployments commonly use a JDK to build and a deliberately small runtime image or JDK distribution to run. The useful interview distinction remains: compilation needs development tools; execution needs a compatible JVM and the application's required runtime libraries.

## Verify the boundary

```text
java -version   -> runtime launcher and version
javac -version  -> compiler availability and version
```

If `java` works but `javac` does not, the environment has a runtime but not a full development toolchain. If compilation works locally but deployment fails, compare the target runtime version and required libraries rather than assuming “JDK versus JRE” is the cause.

## Quick recall

**Q. What is the compact JDK/JRE/JVM answer?**
A. JDK is the development toolchain; runtime/JRE is the libraries plus JVM needed to run; JVM executes class-file bytecode.

**Q. Can a production container run without `javac`?**
A. Yes. It needs a compatible runtime and the application’s dependencies, not a compiler.

**Q. Does “JDK contains JRE contains JVM” always describe a modern download?**
A. It is a useful conceptual nesting, but modular runtime images and vendor packaging mean it is not a deployment-layout guarantee.
