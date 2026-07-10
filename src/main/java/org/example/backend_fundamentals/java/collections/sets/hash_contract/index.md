---
order: 40
---

# Set Hash Contract

`HashSet` deduplicates by the same two-step process as `HashMap` lookup:

1. use `hashCode()` to find a bucket;
2. use `equals()` inside the bucket to confirm equality.

If either method is wrong, the set can contain duplicate-looking values.

```java
class BadKey {
    final String value;
    BadKey(String value) { this.value = value; }
}

Set<BadKey> set = new HashSet<>();
set.add(new BadKey("hello"));
set.add(new BadKey("hello"));
set.add(new BadKey("hello"));

System.out.println(set.size()); // 3
```

Three distinct objects inherit identity-based `equals` and `hashCode`, so the set sees three different elements.

## Fixes

- use a `record` for value objects;
- generate both `equals` and `hashCode` from the same fields;
- do not mutate fields used by either method after insertion.

## Demo code

| Demo | Shows |
| --- | --- |
| `../playground/traps/BrokenEqualsHashCodeSetTrapRun` | Broken contract causes silent duplicates and failed `contains`. |

## Quick recall

- **Can `HashSet` contain duplicate-looking custom objects?** Yes, if equality/hash methods are missing or broken.
- **Fix for simple value key?** Use a record or generate both methods from the same fields.
- **Mutable fields in hash/equality?** Dangerous after insertion.
