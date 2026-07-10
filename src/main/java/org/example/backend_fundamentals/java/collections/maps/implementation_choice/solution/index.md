---
order: 20
search: false
---

# Solutions

## Solution: student-marks-system - Student Marks System

```java
TreeMap<String, Integer> marks = new TreeMap<>();

marks.put("Ravi", 82);
marks.put("Asha", 91);
marks.put("Mina", 88);

marks.replace("Ravi", 86);
System.out.println(marks.get("Asha"));
marks.remove("Mina");

Map.Entry<String, Integer> topper = null;
for (Map.Entry<String, Integer> entry : marks.entrySet()) {
    if (topper == null || entry.getValue() > topper.getValue()) {
        topper = entry;
    }
}

System.out.println("Topper: " + topper);
System.out.println("Alphabetical: " + marks);
```

Use `TreeMap` because the system must print students alphabetically by name. Topper still needs a scan unless you maintain a second index, such as a `PriorityQueue` or `TreeMap<Integer, Set<String>>` by marks.
