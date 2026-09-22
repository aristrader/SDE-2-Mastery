---
title: Interview Questions
order: 20
search: false
---

# JVM Interview Questions

## Question 1: What happens from `.java` source to running code?

Answer shape: `javac` compiles source to bytecode; JVM loads/verifies classes; interpreter starts execution; JIT compiles hot methods; GC manages unreachable heap objects.

## Question 2: Why is Java called platform independent?

Answer shape: Java source is compiled to bytecode, and each OS/CPU has its own JVM implementation that understands that bytecode.

## Question 3: Why can an application still leak memory if Java has GC?

Answer shape: GC collects unreachable objects only. Caches, static collections, listeners, thread-locals, and queues can keep objects reachable forever.

Related full practice: [GC exercise](../../gc/exercise/).

## Question 4: Why can a benchmark be fast after warmup but slow at startup?

Answer shape: the JVM can initially interpret bytecode while it collects profiling data; the JIT later compiles and optimizes hot paths. Compare cold and steady-state behavior instead of treating one timing as universal.

## Quick recall

**Q. What happens before a class is initialized?**
A. It is loaded and linked; linking includes verification, preparation, and resolution, which may be lazy.

**Q. Can two classes with the same binary name be different runtime types?**
A. Yes—if different defining class loaders created them.

**Q. Can GC close a database connection deterministically?**
A. No. Release external resources explicitly with `try-with-resources`.
