---
order: 10
search: false
---

# JMM Practice

## Exercise: visibility-flag - Visibility Flag

### Goal
Understand why one thread's write may not become visible to another thread immediately.

### Task
Create a worker loop controlled by a shared boolean flag.

First use a plain `boolean`. Then change it to `volatile`.

### Checks
- Explain why the plain flag is unsafe.
- Explain what `volatile` changes.
- Explain why `volatile` still does not make compound updates atomic.

## Exercise: static-holder-lazy-initialization - Static Holder Lazy Initialization

### Goal
Understand why the static-holder singleton is lazy and why it does not need `volatile`.

### Task
Use this class shape:

```java
class Singleton {
    static {
        System.out.println("Singleton initialized");
    }

    private Singleton() {
        System.out.println("Singleton constructor");
    }

    private static class Holder {
        static {
            System.out.println("Holder initialized");
        }

        static final Singleton INSTANCE = new Singleton();
    }

    static void touchOuter() {
        System.out.println("touchOuter called");
    }

    static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

Predict the output for each case:

```java
// Case 1
System.out.println("main");
```

```java
// Case 2
System.out.println("main start");
Singleton.touchOuter();
System.out.println("main end");
```

```java
// Case 3
System.out.println("main start");
Singleton.getInstance();
System.out.println("main end");
```

```java
// Case 4
System.out.println("main start");
Singleton.getInstance();
Singleton.getInstance();
System.out.println("main end");
```

```java
// Case 5
System.out.println("main start");
Singleton.touchOuter();
Singleton.getInstance();
System.out.println("main end");
```

### Checks
- Explain why touching the outer class does not initialize `Holder`.
- Explain why `Holder.INSTANCE` is lazy.
- Explain why JVM class initialization safely publishes `INSTANCE`.
- Explain why `volatile` is not needed in this pattern.
