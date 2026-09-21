---
order: 50
---

# Control Flow

## Choose the construct by the decision

- Use `if` / `else if` when predicates differ (`amount > limit`, `user == null`). Order matters: Java takes the first true branch.
- Use `switch` when one selector chooses among named values such as an enum or status. A classic colon-style `switch` falls through unless it reaches `break`, `return`, or `throw`.
- Use `for` when iteration has a counter or update step, `while` when the exit condition is central, and enhanced `for` when only each value matters.

Keep guard conditions narrow and ordered from safe to unsafe:

```java
if (account != null && account.isActive()) {
    sendReminder(account);
}
```

## Enhanced for: traversal, not mutation

```java
for (Employee employee : employees) {
    sendReminder(employee);
}
```

For an `Iterable`, enhanced `for` uses an `Iterator` behind the syntax; for an array, it traverses by index. It is the readable default when the index is irrelevant.

Do not structurally change a normal collection through the collection while that hidden iterator is traversing it. Many JDK iterators are fail-fast and can throw `ConcurrentModificationException`; that detection is best effort, not a thread-safety guarantee.

```java
for (Iterator<String> iterator = names.iterator(); iterator.hasNext();) {
    if (iterator.next().isBlank()) {
        iterator.remove();
    }
}
```

Use `Iterator.remove()` only to remove the element most recently returned by `next()`. Choose `removeIf` when its predicate is all the logic needed.

## Switch: statement versus expression

Arrow rules prevent accidental fall-through and allow grouped labels:

```java
switch (day) {
    case SATURDAY, SUNDAY -> rest();
    default -> work();
}
```

Use a switch expression when the branch computes one value. It must be exhaustive; `default` is a common fallback. A block arm supplies its value with `yield`.

```java
int retryDelaySeconds = switch (status) {
    case RETRYABLE -> 5;
    case THROTTLED -> {
        audit(status);
        yield 30;
    }
    default -> 0;
};
```

`switch` on a `null` selector throws `NullPointerException` unless a modern pattern switch explicitly handles `case null` (Java 21+). Do not present an arrow-style switch as automatically exhaustive: ordinary switch statements can still omit a matching case and do nothing.

---

## Quick recall

- **When does an enhanced `for` use an iterator?** When its expression is an `Iterable`; arrays are traversed by index.
- **Safe way to remove while iterating?** Use `Iterator.remove()` after `next()`, or `removeIf` when a predicate is enough.
- **Why prefer arrow switch rules?** They do not fall through and can group labels.
- **Why are switch expressions safer for a computed value?** Every path must yield a value.
- **`switch (value)` when `value` is `null`?** Usually `NullPointerException`; handle `null` explicitly only with supported pattern-switch syntax.

---
