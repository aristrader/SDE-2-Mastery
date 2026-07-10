---
order: 10
search: false
---

# Practice

## Exercise: broken-set-contract - Broken Set Contract

### Objective
See why `HashSet` needs correct `equals` and `hashCode`.

### Task
Create a custom class with one `String value` field and no `equals`/`hashCode`.
Add three new instances with the same value to a `HashSet`.
Then convert the class to a `record` and repeat.

### Checks
- What is the set size before the fix?
- What is the set size after the fix?
