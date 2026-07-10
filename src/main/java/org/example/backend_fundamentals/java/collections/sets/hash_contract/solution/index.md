---
order: 20
search: false
---

# Solutions

## Solution: broken-set-contract - Broken Set Contract

```java
class BadKey {
    final String value;

    BadKey(String value) {
        this.value = value;
    }
}

Set<BadKey> bad = new HashSet<>();
bad.add(new BadKey("hello"));
bad.add(new BadKey("hello"));
bad.add(new BadKey("hello"));
System.out.println(bad.size()); // 3

record GoodKey(String value) {
}

Set<GoodKey> good = new HashSet<>();
good.add(new GoodKey("hello"));
good.add(new GoodKey("hello"));
good.add(new GoodKey("hello"));
System.out.println(good.size()); // 1
```

The record generates field-based `equals` and `hashCode`, so logically equal values dedupe correctly.
