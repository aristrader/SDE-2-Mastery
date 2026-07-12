---
order: 20
search: false
---

# Stream Intermediate Operations Solutions

## Solution: intermediate-warmup - One Operation at a Time

```java
List<Integer> evens = numbers.stream().filter(n -> n % 2 == 0).toList();
List<Integer> odds = numbers.stream().filter(n -> n % 2 != 0).toList();
List<Employee> highPaid = employees.stream().filter(e -> e.salary() > 100_000).toList();
List<Employee> active = employees.stream().filter(Employee::active).toList();
List<String> nonNull = values.stream().filter(Objects::nonNull).toList();

List<Integer> squares = numbers.stream().map(n -> n * n).toList();
List<String> upper = names.stream().map(String::toUpperCase).toList();
List<String> employeeNames = employees.stream().map(Employee::name).toList();
List<Double> salaries = employees.stream().map(Employee::salary).toList();
List<EmployeeDto> dtos = employees.stream().map(e -> new EmployeeDto(e.id(), e.name())).toList();

List<Integer> flatNumbers = nested.stream().flatMap(List::stream).toList();
List<String> skills = employees.stream().flatMap(e -> e.skills().stream()).toList();
List<String> words = lines.stream().flatMap(line -> Arrays.stream(line.split("\\s+"))).toList();
List<String> tags = rows.stream().flatMap(row -> Arrays.stream(row.split(","))).map(String::trim).toList();

List<Integer> asc = numbers.stream().sorted().toList();
List<Integer> desc = numbers.stream().sorted(Comparator.reverseOrder()).toList();
List<Employee> bySalary = employees.stream().sorted(Comparator.comparingDouble(Employee::salary)).toList();
List<Employee> byDeptName = employees.stream()
    .sorted(Comparator.comparing(Employee::department).thenComparing(Employee::name))
    .toList();
List<Employee> topFive = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .limit(5)
    .toList();

List<Integer> uniqueNumbers = numbers.stream().distinct().toList();
List<String> uniqueStrings = names.stream().distinct().toList();
List<Employee> uniqueEmployees = employees.stream().distinct().toList();

List<Employee> firstTen = employees.stream().limit(10).toList();
List<Employee> topThree = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .limit(3)
    .toList();
List<Employee> afterFive = employees.stream().skip(5).toList();
List<Employee> page = employees.stream().skip((long) page * size).limit(size).toList();

List<String> loggedActiveNames = employees.stream()
    .peek(System.out::println)
    .filter(Employee::active)
    .map(Employee::name)
    .peek(System.out::println)
    .toList();
```

`distinct()` works for custom objects only when equality is implemented correctly.
