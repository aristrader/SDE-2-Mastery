---
order: 40
---

# Platform Independence

Java is portable at the class-file boundary: source compiles to JVM bytecode, and each target platform supplies a compatible JVM implementation.

```text
Java source
  -> javac
  -> JVM bytecode
  -> platform-specific JVM
  -> native machine code
```

The same `.class` file can run on Windows, Linux, and macOS when the runtime supports its class-file version and required Java APIs.

## What portability does not promise

Bytecode portability does not erase dependencies outside bytecode:

- A Java 21 class file will not run on a Java 17 runtime.
- JNI/native libraries, shell commands, file paths, fonts, and OS services are platform-specific.
- Packaging must include the application's dependency graph and compatible configuration.
- CPU architecture is handled by the JVM's own implementation, not by a promise that arbitrary native libraries are portable.

For an interview, distinguish *portable bytecode* from *portable deployment*. The first is Java's JVM contract; the second is an engineering task.

## Quick recall

**Q. Why can Java run cross-platform?**
A. A compatible JVM on each platform executes the same JVM bytecode.

**Q. What can still break after copying the same JAR to another OS?**
A. Runtime version/API mismatch, native dependencies, or environment-specific configuration.

**Q. Is source compatibility enough?**
A. No. The deployed class-file version and runtime libraries must also be compatible.
