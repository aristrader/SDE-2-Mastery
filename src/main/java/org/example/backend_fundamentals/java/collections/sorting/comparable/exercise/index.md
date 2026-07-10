---
order: 10
search: false
---

# Practice

## Exercise: unsorted-custom-object - Try sorting a custom object

### Objective
See why Java cannot sort custom objects without an ordering rule.

### Task
Create a `Student` class with `id`, `name`, and `cgpa`. Create 6 students in an `ArrayList`, print the list, then try `Collections.sort(students)`.

### Checks
- Observe the compile/runtime failure.
- Explain why Java does not know which field should define order.

## Exercise: comparable-natural-order - Natural ordering with Comparable

### Objective
Define one natural order inside the class.

### Task
Make `Student implements Comparable<Student>` and sort naturally by `id` ascending.

### Checks
- `Collections.sort(students)` sorts by id.
- `new TreeSet<>(students)` also iterates by id.
- Use `Integer.compare`, not subtraction.
