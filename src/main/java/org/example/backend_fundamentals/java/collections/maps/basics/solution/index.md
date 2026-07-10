---
order: 20
search: false
---

# Solutions

## Solution: basic-hashmap-operations - Basic HashMap Operations

```java
Map<String, Integer> marks = new HashMap<>();

marks.put("Asha", 90);
marks.put("Ravi", 82);

System.out.println(marks.get("Asha"));              // 90
System.out.println(marks.containsKey("Ravi"));      // true
System.out.println(marks.containsValue(82));        // true
System.out.println(marks.getOrDefault("Mina", 0));  // 0

marks.replace("Ravi", 88);
marks.remove("Asha");

System.out.println(marks.size());
System.out.println(marks.isEmpty());
```

`get()` returns `null` for a missing key unless you use `getOrDefault()`.

## Solution: duplicate-keys - Duplicate Keys

```java
Map<String, Integer> marks = new HashMap<>();

System.out.println(marks.put("Asha", 80)); // null
System.out.println(marks.put("Asha", 95)); // 80

System.out.println(marks.get("Asha"));     // 95
System.out.println(marks.size());          // 1
```

Duplicate keys replace the old value. `put()` returns the previous value, or `null` if no mapping existed.
