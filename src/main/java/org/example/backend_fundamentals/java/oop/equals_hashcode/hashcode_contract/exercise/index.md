---
order: 10
search: false
---

# hashCode Contract Practice

## Exercise: hashcode-override - Override hashCode

### Goal
Implement `hashCode` so it matches `equals`.

### Task
Add `hashCode()` to the `Person` class from the `equals` exercise using the same fields.

### Checks
- If `p1.equals(p2)` is true, then `p1.hashCode() == p2.hashCode()` is also true.
- Changing only a non-identity field should not affect equality or hash code.
- You can explain why equal hash codes do not prove two objects are equal.
