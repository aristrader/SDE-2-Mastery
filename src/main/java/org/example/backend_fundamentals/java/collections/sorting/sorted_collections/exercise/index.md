---
order: 10
search: false
---

# Practice

## Exercise: sorted-collections-with-comparator - TreeSet and PriorityQueue ordering

### Objective
Pass comparators into collections that need ordering.

### Task
Create a `TreeSet<Student>` ordered by `name` and a `PriorityQueue<Student>` where highest `cgpa` is removed first.

### Checks
- TreeSet iteration follows name order.
- PriorityQueue `poll()` returns students by descending CGPA.

## Exercise: treemap-by-student-id - TreeMap ordered by Student id

### Objective
Use ordered custom keys.

### Task
Create a `TreeMap<Student, Integer>` ordered by student id. Store marks as the value.

### Checks
- Print the map.
- Verify keys appear in id order.

## Exercise: treeset-compareto-zero-trap - TreeSet duplicate trap

### Objective
See how `compareTo()` returning `0` affects sorted sets.

### Task
Create two students with the same id and different names. Use natural ordering that compares only id. Insert both into a `TreeSet`.

### Checks
- Observe how many objects exist.
- Explain why one object is treated as duplicate.
