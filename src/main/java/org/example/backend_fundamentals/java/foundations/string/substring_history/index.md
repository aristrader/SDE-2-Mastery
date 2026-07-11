---
order: 40
---

# substring History

In Java 6 and earlier, `substring()` could retain the original backing character array.

```java
String huge = readEntireFile();
String tiny = huge.substring(0, 5);
huge = null;
```

Historically, `tiny` could keep the whole large array alive. Java 7u6 changed `substring()` to copy the relevant characters, so this is mostly an interview/history point now.

## Quick recall

- **Old trap?** Small substring retained a large backing array.
- **Fixed when?** Java 7u6.
- **Modern practical issue?** Mostly historical, but still useful for interviews.
