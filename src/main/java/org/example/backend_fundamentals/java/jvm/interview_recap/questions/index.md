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
