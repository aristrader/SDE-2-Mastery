---
order: 30
---

# Generics

Generics let you write type-safe, reusable code without duplicating logic per type. The compiler enforces type constraints at compile time; at runtime, the type parameters are gone (type erasure).

---

## Topic 1 — Type parameters (basics)

A type parameter is a placeholder — `T`, `A`, `B` — declared in angle brackets and substituted at the call site.

```java
public class Box<T> {
    private T value;
    public void set(T value) { this.value = value; }
    public T get() { return value; }
}

Box<String> box = new Box<>();
box.set("hello");
String s = box.get();  // no cast needed
```

**Multiple type parameters:**

```java
public class Pair<A, B> {
    private final A first;
    private final B second;
    public Pair<B, A> swap() { return new Pair<>(second, first); }
}
```

**Why generics over raw types:**

```java
List names = new ArrayList();     // raw — compiles, crashes at runtime
names.add(5);                     // no compile error
String s = (String) names.get(0); // ClassCastException

List<String> safe = new ArrayList<>();
safe.add(5);                      // COMPILE ERROR — bug caught immediately
String s = safe.get(0);           // no cast needed
```

---

## Topic 2 — Bounds (`extends`)

Without bounds, `T` is just `Object`. Bounds constrain `T`, unlocking methods from the bound type.

```java
// T must be a Number — can call .doubleValue()
public static <T extends Number> double sum(List<T> list) {
    double total = 0;
    for (T item : list) { total += item.doubleValue(); }
    return total;
}

// Multiple bounds — T must satisfy both
public static <T extends Number & Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;
}
```

`extends` is the only keyword for bounds on type parameters — there is no `<T super X>`.

**Erasure with multiple bounds:** always erases to the **leftmost** bound. `<T extends Number & Comparable<T>>` → erases to `Number`; the compiler inserts casts where `Comparable` methods are called. `Number` is a class (T extends it), `Comparable` is an interface (T implements it) — but `extends` is used for both in generic bounds.

**Common trap:**

```java
public <T> T findMax(List<T> list) {
    T max = list.get(0);
    if (item > max) { ... }  // COMPILE ERROR — > only works on primitives, not objects
}
// Fix: <T extends Comparable<T>> and use compareTo() > 0
```

---

## Topic 3 — Wildcards

Wildcards (`?`) are bounds with the type name thrown away. Use when you don't need to refer to the type elsewhere in the method.

### Unbounded — `?`

```java
void printAll(List<?> list)   // accepts List<String>, List<Integer>, anything
```

Same as `<T> void printAll(List<T> list)` without naming T — cleaner when T is unused.

### Upper bounded — `? extends T`

"Some type that is T or a subtype of T." Read-safe, write-blocked.

```java
double sumList(List<? extends Number> list)  // accepts List<Integer>, List<Double>, etc.
```

- `list.get(i)` → safe, guaranteed to be at least a `Number`
- `list.add(5)` → COMPILE ERROR — list might be `List<Double>` at runtime; adding `Integer` would corrupt it

### Lower bounded — `? super T`

"Some type that is T or a supertype of T" — think: **T or above**. Write-safe, reads give `Object`.

```java
void addNumbers(List<? super Integer> list)  // accepts List<Integer>, List<Number>, List<Object>
```

- `list.add(5)` → safe — the list is `Integer` or something above it; `Integer` is a subtype of all possible actual types, so it always fits
- `Integer x = list.get(0)` → COMPILE ERROR — list might be `List<Number>` or `List<Object>`; compiler can only promise `Object`

**Common confusion:** `? super Integer` does NOT mean "at most an Integer." It means the list's actual type is Integer or higher up the hierarchy (Number, Object). You're writing *down* into it, not reading *up* from it.

### Wildcards vs bounds — when to use which

| Need to name the type again? | Use |
|---|---|
| No | Wildcard (`?`, `? extends`, `? super`) |
| Yes (return type, another parameter, etc.) | Named bound (`<T extends ...>`) |

`? super` only exists for wildcards — there is no `<T super X>` named bound.

### Invariance

`List<Integer>` is **not** a `List<Number>` — generics are invariant. Wildcards restore the is-a relationship:

```java
List<Number> wrong = ints;              // COMPILE ERROR — invariance
List<? extends Number> ok = ints;       // works — wildcard restores is-a
```

**Why invariance exists — the corruption scenario:** imagine if it were allowed:

```java
List<Integer> ints = new ArrayList<>();
List<Number> nums = ints;   // pretend this compiled
nums.add(3.14);             // Double is a Number — looks legal
Integer x = ints.get(0);   // ClassCastException — it's actually a Double!
```

`nums` and `ints` point to the same list. Adding a `Double` through `nums` corrupts `ints`. The compiler blocks this at the assignment rather than letting you reach that point. The wildcard fix `List<? extends Number>` works because `add` is then blocked entirely — the corruption path no longer exists.

Note: arrays are **covariant** (`Integer[]` IS-A `Number[]`), which is why array corruption bugs can still happen at runtime. Generics chose invariance to eliminate this class of bug at compile time.

---

## Topic 4 — PECS

**Producer Extends, Consumer Super** — the rule for choosing which wildcard to use.

| Your method does this with the list | List role | Wildcard |
|---|---|---|
| Reads values out | Producer | `? extends T` |
| Writes values in | Consumer | `? super T` |
| Both | Neither | Named bound `<T>` |

**Canonical example — `Collections.copy`:**

```java
public static <T> void copy(List<? super T> dst, List<? extends T> src) {
    for (T item : src) {  // src produces values → extends
        dst.add(item);    // dst consumes values → super
    }
}
```

`copy(dst, src)` accepts `copy(List<Number> dst, List<Integer> src)` — works because `Integer extends Number`.

---

## Topic 5 — Type Erasure

Generics are compile-time only. The compiler **first checks** all type parameters for correctness, **then strips** them before producing bytecode. Erasure happens after type checking — not during. This matters: overloading failures (see consequence 4 below) are caught because the compiler sees what the bytecode *would* look like and rejects duplicates ahead of time.

**What erasure does:**

| At compile time | In bytecode |
|---|---|
| `List<String>` | `List` |
| `<T>` | `Object` |
| `<T extends Number>` | `Number` |
| `T value` | `Object value` |

The compiler inserts casts automatically where needed:

```java
// Source
List<String> list = new ArrayList<>();
String s = list.get(0);

// Bytecode equivalent
List list = new ArrayList();
String s = (String) list.get(0);  // compiler inserted
```

**Why:** Backwards compatibility. Generics were added in Java 5; existing code used raw types. Erasure meant the JVM didn't need to change — old bytecode keeps running on new JVMs. This is also why raw types can never be removed.

**Consequences:**

```java
// 1. instanceof with parameterized type — COMPILE ERROR
if (list instanceof List<Integer>) { }  // type info gone at runtime
if (list instanceof List) { }           // correct — raw type check

// 2. Generic array creation — COMPILE ERROR
T[] arr = new T[10];  // JVM doesn't know what T is

// 3. Instantiating T — COMPILE ERROR
T obj = new T();  // JVM doesn't know which constructor to call

// 4. Overloading on generic types — COMPILE ERROR
void process(List<Integer> list) { }
void process(List<String> list) { }   // both erase to process(List) — duplicate
```

**Proof erasure happens — both lists are the same class:**

```java
List<Integer> ints = new ArrayList<>();
List<String> strs = new ArrayList<>();
System.out.println(ints.getClass() == strs.getClass());  // true
```

---

## Quick recall

**Q. What does `<T extends Number & Comparable<T>>` mean?**
A. T must be both a Number and Comparable. Erases to Number (leftmost bound).

**Q. Why can't you add to a `List<? extends Number>`?**
A. The list might be a `List<Double>` at runtime — adding an Integer would corrupt it. Compiler refuses all adds.

**Q. Why do reads from `List<? super Integer>` give Object?**
A. The list might be `List<Number>` or `List<Object>` — the compiler can only guarantee Object.

**Q. PECS in one line?**
A. If you read from it use `extends`, if you write into it use `super`, if both use a named bound.

**Q. Why can't you do `list instanceof List<String>`?**
A. Type erasure — at runtime `List<String>` is just `List`. The JVM has no type parameter to check.

**Q. Why can't raw types be removed from Java?**
A. Backwards compatibility — pre-Java 5 bytecode uses raw types and must keep running on new JVMs.

**Q. Why is `List<Integer>` not a `List<Number>` even though Integer extends Number?**
A. Generics are invariant. If it were allowed, you could add a `Double` through `List<Number>` and corrupt the underlying `List<Integer>`. The compiler blocks the assignment instead.

**Q. What does `? super Integer` actually mean — "at most Integer" or "Integer or above"?**
A. Integer or above. The list's actual type is `Integer`, `Number`, or `Object`. You write Integer *into* it (safe because Integer fits all supertypes); reads give only `Object` because the actual type could be anything up the hierarchy.
