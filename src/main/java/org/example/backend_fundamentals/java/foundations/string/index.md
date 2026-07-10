---
order: 30
---

# String Immutability

> One of the most interview-dense topics in Java. Touches heap, string pool, final, hashCode caching, security, and StringBuilder. Most candidates know "String is immutable" but can't explain why or what the consequences are.

---

## What immutability means for String

Once a `String` is created, its character sequence cannot be changed. No method modifies the underlying `char[]` — every operation that looks like a modification (`concat`, `replace`, `toUpperCase`, `substring`) returns a **new** `String`.

```java
String s = "hello";
s.toUpperCase();         // returns a new String — s is unchanged
System.out.println(s);  // "hello"

s = s.toUpperCase();     // now s points to a new object "HELLO"
```

The original `"hello"` on the heap is untouched. `s` is a reference — it got reassigned.

---

## Where Strings live — heap vs string pool

The heap is where all Java objects live. The string pool is a sub-region *inside* the heap.

Before Java 7, the pool lived in **PermGen** — a fixed-size region outside the heap, which caused `OutOfMemoryError: PermGen space` when too many strings were interned. Java 7 moved it into the heap so it's subject to normal GC and can grow freely.

| | Heap | String Pool |
|---|---|---|
| What lives here | All objects, any type | Only `String` objects |
| Deduplication | No | Yes — one object per unique value |
| Location | Top-level JVM memory region | Sub-region inside the heap (Java 7+) |
| GC'd? | Yes, when unreachable | Yes, same rules as heap |

Java maintains the pool by checking it on every string literal creation.

**String literals → pool:**
```java
String a = "hello";  // JVM checks pool — not there, creates "hello" in pool, a points to it
String b = "hello";  // JVM checks pool — found, b points to the same object
System.out.println(a == b); // true — same object
```

**`new String()` → always a new heap object, bypasses the pool:**
```java
String c = new String("hello"); // creates a brand-new object on the heap
System.out.println(a == c);     // false — different objects
System.out.println(a.equals(c));// true — same content
```

**Memory diagram:**

```
Heap
├── String Pool
│   └── "hello"  ←── a, b both point here
└── (regular heap)
    └── "hello"  ←── c points here (separate object, same content)
```

---

## Why String is immutable — the four reasons

### 1. String pool safety

**Why was the pool created?** Strings are the most heavily used objects in any Java program — class names, config keys, log messages, HTTP headers, DB queries. Without the pool, every identical literal would be a separate heap object. The pool deduplicates: one object, many references.

```java
// Without pool: 3 separate heap objects
String a = new String("hello");
String b = new String("hello");
String c = new String("hello");

// With pool: one object, three references
String a = "hello";
String b = "hello";
String c = "hello"; // a == b == c → true
```

Sharing only works if content never changes. If `String` were mutable, one holder could corrupt everyone else pointing to the same object:

```java
String a = "hello";
String b = "hello"; // same pooled object
// If String were mutable:
a.setCharAt(0, 'X'); // b would now also be "Xello" — disaster
```

The causality chain: **performance need → pool (sharing) → immutability required to make sharing safe.** The pool wasn't born from immutability; immutability was the prerequisite the pool demanded.

### 2. Security

`String` is used for class names, file paths, network URLs, database connection strings, and credentials. The JVM itself uses `String` for class loading.

If `String` were mutable, an attacker could pass a string through a security check, then mutate it before the operation:

```java
String path = "/safe/path";
checkPermission(path);  // passes
path.mutate("/etc/passwd"); // hypothetical — now dangerous
openFile(path);          // too late
```

Immutability means a `String` that passed a check cannot change afterwards.

### 3. hashCode caching

`String` computes its `hashCode` lazily on first call and then caches it in a private field:

```java
// Inside String.java (simplified)
private int hash; // defaults to 0

public int hashCode() {
    if (hash == 0 && !isEmpty()) {
        hash = computeHash(); // expensive — only happens once
    }
    return hash;
}
```

This is safe **only because** content never changes. If `String` were mutable, the cached hash would go stale on mutation — HashMap / HashSet would silently break (object in wrong bucket, never findable again).

### 4. Thread safety

Immutable objects are inherently thread-safe. Multiple threads can read the same `String` with no synchronization. If `String` were mutable, every read would need a lock.

---

## Why String is `final`

**Important distinction: `final` did not create immutability. The internal design did.**

Immutability comes from `private char[]` + no mutating methods. A class can be immutable without being `final`:

```java
public class Point {           // not final — but still immutable
    private final int x;
    private final int y;
    Point(int x, int y) { this.x = x; this.y = y; }
    // no setters, no way to change x or y
}
```

So why is `String` `final`? Without `final`, a subclass could break the immutability contract while still being a valid `String` reference:

```java
String s = new EvilString("hello"); // valid — EvilString IS-A String
cache.put("key", s);                // stored as String, trusted as immutable
s.mutate();                         // changes content — pool, hashCode, security all broken
```

The caller held a `String` reference and trusted the contract; the subclass violated it from underneath.

**The correct mental model:**
- `private char[]` + no mutating methods → **creates** immutability
- `final class` → **protects** immutability from subclass subversion

`final` is the lock on the door — the room was already built secure. See also: `Inheritance.md` — the `final class` section.

---

## `+` operator and `StringBuilder` under the hood

`String` concatenation with `+` looks innocent but creates intermediate objects:

```java
// Naive loop — creates N intermediate String objects
String result = "";
for (int i = 0; i < 1000; i++) {
    result = result + i; // new String every iteration
}
```

The JVM (since Java 9, using `invokedynamic` + `StringConcatFactory`) optimises **single-statement** concatenation:

```java
String s = "Hello " + name + "!"; // JVM compiles to one efficient call
```

But **loop concatenation** still creates garbage. Use `StringBuilder`:

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i); // mutates internal buffer — no intermediate String
}
String result = sb.toString(); // one String at the end
```

| | `String` | `StringBuilder` |
|---|---|---|
| Mutable? | No | Yes |
| Thread-safe? | Yes (immutable) | No |
| Use when | value won't change | building strings in a loop or across steps |

Use `StringBuilder` for all string building. If you need thread safety, handle it at a higher level (e.g., confine the builder to one thread).

---

## `substring()` — the Java 6 memory leak trap

In Java 6 and earlier, `substring()` did **not** copy the underlying `char[]`. It created a new `String` sharing the same `char[]` with an offset + length. A small substring kept the entire original array alive.

```java
// Java 6 trap:
String huge = readEntireFile(); // 10MB char[]
String tiny = huge.substring(0, 5); // still holds reference to 10MB array
huge = null; // GC can't collect — tiny's char[] IS the 10MB array
```

**Fixed in Java 7u6:** `substring()` copies the relevant portion into a new `char[]`. Still comes up in interviews.

---

## Common interview questions

**Q: Why does `"hello" == "hello"` return true but `new String("hello") == new String("hello")` return false?**

Literals are interned — both variables point to the same pooled object. `new String()` bypasses the pool — two separate heap objects. Use `.equals()` for content comparison.

**Q: Can you make a String change its value using reflection?**

Technically yes — access the private `value` field via reflection and modify it. This breaks the hashCode cache, pool safety, and security guarantees. Undefined behaviour in practice, and broken in newer JVMs by strong encapsulation (`--add-opens` restrictions). Never do it.

**Q: Is String thread-safe?**

Yes, because it's immutable. No synchronization needed for reads.

**Q: What happens to the old String when you do `s = s + "world"`?**

A new `String` is created with the concatenated value; `s` points to it. The old object becomes unreachable (if no other reference exists) and is eligible for GC. If the old string was a literal, it stays in the pool — pool objects are GC roots.

**Q: Why is `StringBuilder` not thread-safe?**

Its internal `char[]` buffer is mutable and no method is synchronized. Two threads calling `append()` simultaneously can corrupt the buffer. Confine `StringBuilder` to a single thread (the common case), or synchronize externally. `StringBuffer` — the old synchronized alternative — is legacy and should not be used in new code.

---

## Quick recall

**Q. Why is String immutable?**
A. Four reasons: (1) string pool safety — sharing requires content never changes; (2) security — string used for paths/credentials can't be mutated after a security check; (3) hashCode caching — cached hash only valid if content is fixed; (4) thread safety — immutable objects need no locks.

**Q. String literal vs `new String()` — heap difference?**
A. Literal goes into the string pool (shared). `new String()` always creates a new heap object, bypassing the pool. Use `.equals()`, never `==`, for content comparison.

**Q. When to use StringBuilder?**
A. Any time you're building a String in a loop or across multiple steps in a single thread. `+` in a loop creates intermediate garbage; `StringBuilder.append()` mutates a buffer and calls `toString()` once at the end.

**Q. Java 6 `substring()` trap?**
A. Pre-Java-7u6, `substring()` shared the original `char[]`, keeping the whole backing array alive. Fixed in Java 7u6 — `substring()` now copies. If you're ever asked about String memory leaks, this is the historical answer.
