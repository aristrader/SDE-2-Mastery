---
order: 40
---

# substring History

This is a versioned diagnostic, not a modern micro-optimization rule. In Java 6 and earlier, `substring()` could retain the original backing character array.

```java
String huge = readEntireFile();
String tiny = huge.substring(0, 5);
huge = null;
```

Historically, `tiny` could keep the whole large array alive. Java 7u6 changed `substring()` to copy the relevant characters, so a small modern substring does not retain its source merely because of `substring()`.

## How to answer the follow-up

The old implementation saved copying by sharing storage; the failure was accidental retention of a large source through a tiny slice. Modern Java pays the copy at `substring()` instead. If a current service retains memory, profile the actual retained objects and references before blaming `substring()`; a legacy interview fact is not a production diagnosis.

## Quick recall

- **Old trap?** Small substring retained a large backing array.
- **Fixed when?** Java 7u6.
- **Modern practical issue?** Mostly historical; profile current retention instead of applying the old workaround.
