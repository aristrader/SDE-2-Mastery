---
order: 20
search: false
---

# StringBuilder Solutions

## Solution: stringbuilder-loop - StringBuilder in Loops

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);
}
String result = sb.toString();
```

`StringBuilder` mutates one internal buffer. Repeated `s += i` creates a new string on each iteration and copies previous content.

If separators are needed, avoid a trailing-delimiter cleanup pass:

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    if (i > 0) {
        sb.append(',');
    }
    sb.append(i);
}
String result = sb.toString();
```

Keep `sb` method-local. The safe result to return or share is `result`; `StringBuilder` is mutable and not thread-safe.
