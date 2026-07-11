---
order: 20
search: false
---

# finally and Resources Solutions

## Solution: finally - finally

```java
try {
    System.out.println("try");
    // throw new RuntimeException("boom");
    // return;
} finally {
    System.out.println("finally always runs before leaving");
}
```

`finally` runs before control leaves the method or before the exception continues upward.

## Solution: return-in-finally - return in finally

The answer is `2` because `finally` runs after the `try` block but before control returns to the caller. A `return` in `finally` replaces the pending return from `try`.

That is why returning from `finally` is bad practice: it can also suppress a pending exception.

## Solution: try-with-resources - try-with-resources

```java
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("test.txt"));
    System.out.println(reader.readLine());
} finally {
    if (reader != null) {
        reader.close();
    }
}

try (BufferedReader reader = new BufferedReader(new FileReader("test.txt"))) {
    System.out.println(reader.readLine());
}
```

Try-with-resources is shorter, closes reliably, closes in reverse creation order, and keeps close failures as suppressed exceptions instead of losing them.

## Solution: try-with-resources-warmup - Warm-up (close ordering)

```java
final class Resource implements AutoCloseable {
    private final String name;

    Resource(String name) {
        this.name = name;
        System.out.println("Opening Resource " + name);
    }

    void use() {
        System.out.println("Using Resource " + name);
    }

    @Override
    public void close() {
        System.out.println("Closing Resource " + name);
    }
}

try (Resource r1 = new Resource("r1");
     Resource r2 = new Resource("r2")) {
    r1.use();
    r2.use();
}
```

`r2` closes before `r1`. Java closes resources in reverse declaration order, like unwinding a stack.

## Solution: try-with-resources-exception - Close with exception

```java
final class Resource implements AutoCloseable {
    private final String name;

    Resource(String name) {
        this.name = name;
    }

    void use() {
        System.out.println("Using Resource " + name);
    }

    @Override
    public void close() {
        if ("r1".equals(name)) {
            throw new RuntimeException("Close failed: r1");
        }
    }
}

try (Resource r1 = new Resource("r1")) {
    r1.use();
    throw new RuntimeException("Body failed");
} catch (RuntimeException ex) {
    System.out.println(ex.getMessage());
    System.out.println(ex.getSuppressed()[0].getMessage());
}
```

The body exception is primary because it is the original failure. The close failure is attached as suppressed so it is not lost.
