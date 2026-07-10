---
order: 20
search: false
---

# Solutions

Use this compact model:

```java
record Student(int id, String name, double cgpa) {
}
```

## Solution: simple-comparators - Single-field Comparator sorting

```java
Comparator<Student> byName = Comparator.comparing(Student::name);
Comparator<Student> byCgpaDesc = Comparator.comparingDouble(Student::cgpa).reversed();
Comparator<Student> byIdDesc = Comparator.comparingInt(Student::id).reversed();

students.sort(byName);
students.sort(byCgpaDesc);
students.sort(byIdDesc);
```

Without method references:

```java
Comparator<Student> byName = Comparator.comparing(student -> student.name());
Comparator<Student> byCgpaDesc = Comparator
    .comparingDouble(student -> student.cgpa())
    .reversed();
```

## Solution: comparator-chaining - Chain multiple comparator rules

```java
Comparator<Student> byCgpaNameId =
    Comparator.comparingDouble(Student::cgpa).reversed()
        .thenComparing(Student::name)
        .thenComparingInt(Student::id);

students.sort(byCgpaNameId);
```

## Solution: employee-comparator-strategy - Employee comparator strategy

```java
record Employee(int id, String name, long salary, int age, String department) {}

Comparator<Employee> bySalary = Comparator.comparingLong(Employee::salary);
Comparator<Employee> byAge = Comparator.comparingInt(Employee::age);
Comparator<Employee> byDepartment = Comparator.comparing(Employee::department);
Comparator<Employee> byDepartmentSalaryName =
    Comparator.comparing(Employee::department)
        .thenComparingLong(Employee::salary)
        .thenComparing(Employee::name);
```

`Employee` has many valid business orderings, so `Comparator` is better than pretending one order is natural.
