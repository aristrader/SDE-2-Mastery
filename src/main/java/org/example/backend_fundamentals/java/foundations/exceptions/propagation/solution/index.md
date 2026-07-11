---
order: 20
search: false
---

# Checked Handling Solutions

## Solution: checked-exception - Checked Exception

```java
static void readFileWithThrows() throws IOException {
    FileReader reader = new FileReader("test.txt");
}

static void readFileWithCatch() {
    try {
        FileReader reader = new FileReader("test.txt");
    } catch (IOException e) {
        System.out.println("File not found");
    }
}
```

`FileReader` can throw `FileNotFoundException`, a checked exception, so the method must catch or declare it.

## Solution: exception-propagation - Exception Propagation

```java
static void c() throws IOException {
    throw new IOException("failed in c");
}

static void b() throws IOException {
    c();
}

static void a() throws IOException {
    b();
}

public static void main(String[] args) {
    try {
        a();
    } catch (IOException e) {
        System.out.println("Caught in main: " + e.getMessage());
    }
}
```

If `b` or `a` catches the exception, the methods above it no longer need to declare `throws IOException`.
