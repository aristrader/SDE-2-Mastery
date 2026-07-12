---
order: 10
search: false
---

# Stream Interview Drills Practice

Use the common `Employee` model.

## Exercise: mixed-stream-pipelines - Mixed Stream Questions

### Goal
Combine multiple stream operations in realistic interview prompts.

### Task
Solve these:

1. Find names of all active employees.
2. Find names of top 5 highest-paid active employees.
3. Find departments having more than 5 employees.
4. Find all unique skills across all employees.
5. Count employees having the `"Java"` skill.
6. Find average salary of active employees.
7. Find employees sorted by salary descending.
8. Find department with the maximum number of employees.
9. Find employee names department-wise.
10. Find the highest-paid active employee.
11. Find total salary department-wise.
12. Find duplicate department names.
13. Create `department -> average salary`.
14. Create `department -> employee count`.
15. Create `department -> list of employee names`.

## Exercise: output-prediction - Predict Stream Output

### Goal
Mentally execute stream pipelines.

### Task
Predict these outputs:

1. `List.of(1,2,3,4).stream().filter(x -> x % 2 == 0).map(x -> x * 10).toList()`
2. `List.of("a","b","a","c").stream().distinct().sorted().toList()`
3. `List.of(1,2,3,4,5).stream().skip(2).limit(2).toList()`
4. `List.of(List.of(1,2), List.of(3,4)).stream().flatMap(List::stream).toList()`
5. `List.of(2,4,6,8).stream().allMatch(x -> x % 2 == 0)`
6. `List.of(2,4,5,8).stream().noneMatch(x -> x % 2 == 0)`

## Exercise: find-stream-bugs - Find the Bug

### Goal
Recognize common stream bugs.

### Task
Explain what is wrong:

1. `Collectors.toMap(Employee::getId, Function.identity())` when duplicate IDs exist.
2. Reusing a stream after `stream.count()`.
3. `employees.stream().findFirst().get()`.
4. `employees.parallelStream().forEach(list::add)`.
5. `.peek(e -> e.setSalary(100000))`.

## Exercise: loop-to-streams - Convert Loops to Streams

### Goal
Rewrite common loop patterns as streams.

### Task
Convert loops that:

1. Filter active employees.
2. Extract employee names.
3. Compute total salary.
4. Group employees by department.
5. Find the highest-paid employee.
6. Remove duplicate department names.

## Exercise: choose-stream-api - Choose the Correct API

### Goal
Pick the smallest correct stream API.

### Task
For each requirement, name the API:

1. Remove unwanted elements.
2. Convert `Employee` into `EmployeeDTO`.
3. Flatten employee skills.
4. Build a `Map<Id, Employee>`.
5. Group employees by department.
6. Divide employees into active/inactive.
7. Find whether at least one employee belongs to HR.
8. Calculate total salary.
9. Calculate average salary.
10. Find first matching employee.
11. Find any matching employee.
12. Remove duplicates.
13. Get top 10 records.

## Exercise: stream-challenges - Challenge Problems

### Goal
Build longer pipelines after the basic operations are comfortable.

### Task
Solve these:

1. Return names of the top 10 highest-paid active employees grouped by department.
2. Find all unique skills department-wise.
3. Find employees sharing at least one common skill with another employee.
4. Build `department -> highest paid employee name`.
5. Build `department -> average salary`, sorted descending.
6. Given sentences, return the top 10 most frequent words.
7. Build a frequency map of employee skills.
8. Find the department with the highest average salary.
9. Find employees whose skills include both `"Java"` and `"Spring"`.
10. Return names of top 5 highest-paid active IT employees whose salary is above the department average.
