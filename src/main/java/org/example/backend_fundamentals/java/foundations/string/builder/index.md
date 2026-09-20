---
order: 30
---

# StringBuilder

Use a local `StringBuilder` when the final text is assembled through repeated, data-dependent appends. It keeps a mutable buffer while building, then publishes one immutable `String`.

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);
}
String result = sb.toString();
```

## The repeated-copy failure

Loop concatenation repeatedly replaces an immutable value. The old content must be copied into each next result, so work and temporary allocation grow sharply as the result grows:

```java
String result = "";
for (int i = 0; i < 1000; i++) {
    result = result + i;
}
```

`StringBuilder` appends into one growable buffer, then `toString()` creates the final immutable value. If the final size is reliably known, give the builder an initial capacity to avoid buffer growth:

```java
StringBuilder sql = new StringBuilder(128);
```

Do not guess a capacity or retain a giant builder globally just to avoid a small allocation.

## Choose the smallest readable tool

| Need | Choice | Boundary |
| --- | --- | --- |
| Fixed, readable expression | `"user=" + userId` | Do not introduce a builder for one expression. |
| Loop or conditional assembly | local `StringBuilder` | Call `toString()` at the boundary. |
| Delimited values | `String.join` or `Collectors.joining` | Avoid manual delimiter cleanup. |
| Shared mutable buffer | redesign to build locally and publish `String` | `StringBuilder` is not thread-safe; `StringBuffer` synchronizes methods but does not simplify ownership. |

## Recovery when a builder escaped

If a field or shared object exposes a `StringBuilder`, do not add scattered `synchronized` blocks first. Move construction into the owning method, return a `String`, and share that immutable result. Use `StringBuffer` only when the mutable buffer itself truly must be shared and its whole protocol is synchronized.

## Quick recall

- **Loop string building?** Use `StringBuilder`.
- **Thread-safe?** No; confine it to one thread.
- **Final output?** Call `toString()`.
- **Use it for one `+` expression?** Usually no; keep the expression readable.
- **How to share built text?** Publish the resulting immutable `String`, not the builder.
