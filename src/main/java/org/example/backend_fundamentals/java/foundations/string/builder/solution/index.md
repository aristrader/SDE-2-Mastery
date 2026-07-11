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
