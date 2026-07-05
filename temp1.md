# Java Interview Notes Dump - Object Class Module (Part 2)

---

# equals() and hashCode() Contract (Deep Dive)

This was identified as one of the most important Java Collections interview questions.

Suppose:

```java
class Employee {

    int id;

}
```

Create two objects:

```java
Employee e1 = new Employee(1);
Employee e2 = new Employee(1);
```

Assume equals() has been overridden so that:

```text
e1.equals(e2) == true
```

Now imagine only equals() is overridden.

Default hashCode() still exists.

Possible hashes:

```text
e1 -> 1023

e2 -> 6789
```

HashSet stores:

```text
Bucket 1023

↓

e1
```

Later:

```java
HashSet.contains(e2);
```

HashSet computes:

```text
6789
```

It immediately looks in:

```text
Bucket 6789
```

Nothing is there.

Important point:

equals() is **never even called**, because HashSet looked in the wrong bucket.

Result:

```text
contains()

↓

false
```

even though:

```text
equals()

↓

true
```

Therefore Java defines a contract.

---

## equals()/hashCode() Contract

If:

```text
a.equals(b)

↓

true
```

then:

```text
a.hashCode()

MUST equal

b.hashCode()
```

Otherwise hash-based collections break.

The reverse is **not** required.

Example:

```text
hashCode()

↓

same
```

does **not** imply

```text
equals()

↓

true
```

because collisions are allowed.

---

## Interview Rule to Memorize

Equal objects:

```text
MUST

↓

same hashCode
```

Unequal objects:

```text
MAY

↓

same hashCode
```

---

# Mutable Objects as HashMap Keys

## User Explanation

Suppose a mutable object is used as a key.

HashMap computes its hashCode and places it into a bucket.

Later, someone modifies the object.

Now its hashCode changes.

Searching for the same object fails because HashMap searches using the new hashCode while the object still exists in the old bucket.

### Review

Correct.

Expanded visualization:

Initially:

```text
Employee(id=10)

↓

hash = 21

↓

Bucket 21
```

HashMap stores:

```text
Bucket 21

↓

Employee(id=10)
```

Later:

```text
id = 50
```

Now:

```text
hash = 77
```

Searching:

```java
map.get(employee)
```

HashMap computes:

```text
77
```

Looks inside:

```text
Bucket 77
```

The object physically still exists in:

```text
Bucket 21
```

Result:

```text
Not Found
```

even though the object is literally present inside the map.

This is why mutable objects are dangerous as HashMap keys.

---

# Why String is a Good HashMap Key

## User Answer

Because String is immutable.

### Expanded Explanation

Correct.

Reasons discussed:

- Immutable
- Proper equals()
- Proper hashCode()
- Cached hashCode

Example:

```text
"Swapnil"
```

cannot later become

```text
"Rahul"
```

Therefore its bucket never changes after insertion.

---

# HashSet Uses Both hashCode() and equals()

## User Understanding

HashSet contains hash buckets.

hashCode() determines the bucket.

equals() determines whether two objects are actually equal.

### Review

Correct.

---

# Comparable vs Comparator (Deep Discussion)

Interview scenario:

Employee:

```java
class Employee {

    int id;
    String name;
    int salary;

}
```

Interviewer:

"Sort employees."

Immediate follow-up:

Sort by:

- id?
- salary?
- name?
- joining date?

There is no single answer.

This is why Java has two mechanisms.

---

## Comparable

Purpose:

Natural ordering.

Example:

```java
class Employee implements Comparable<Employee> {

    @Override
    public int compareTo(Employee e) {
        return this.id - e.id;
    }

}
```

Now:

```java
Collections.sort(list);
```

automatically sorts by id.

Concept:

The class itself says:

"I know how I should normally be sorted."

Only one natural ordering exists.

---

## Comparator

Purpose:

External/custom ordering.

Examples:

Sort by salary:

```java
Comparator<Employee> salaryComparator =
    (a,b) -> a.salary - b.salary;
```

Sort by name:

```java
Comparator<Employee> nameComparator =
    Comparator.comparing(Employee::getName);
```

Usage:

```java
Collections.sort(list, salaryComparator);
```

Later:

```java
Collections.sort(list, nameComparator);
```

No need to modify Employee.

Concept:

"I know how I want to sort this class."

---

# Comparable vs Comparator Table

| Comparable | Comparator |
|------------|------------|
| Inside class | Separate class/object |
| compareTo() | compare() |
| Natural ordering | Custom ordering |
| One ordering | Unlimited orderings |
| Collections.sort(list) | Collections.sort(list, comparator) |

---

# Making an Immutable Class

## User Answer

- Make class final.
- Make fields private.
- Make fields final.
- Provide no setters.

### Correction

Very good answer.

One missing point:

Defensive copying.

Example:

```java
class Student {

    private final Date dob;

}
```

Wrong constructor:

```java
this.dob = dob;
```

Problem:

Caller still holds the original Date reference.

Later:

```java
dob.setTime(...)
```

Student object changes.

Correct constructor:

```java
this.dob = new Date(dob.getTime());
```

Correct getter:

```java
return new Date(dob.getTime());
```

This prevents callers from mutating internal state.

---

# == vs equals()

User said this topic was already covered.

Summary retained:

Primitives:

```java
int a = 5;
int b = 5;

a == b
```

compares values.

Objects:

```java
Employee e1;
Employee e2;
```

```java
e1 == e2
```

compares references.

```java
e1.equals(e2)
```

performs logical equality according to equals() implementation.

---

# @Override Annotation

## User Understanding

Always use @Override because:

- It prevents spelling mistakes.
- It ensures a method is actually overriding something.
- Otherwise the build fails if no matching parent method exists.

### Review

Correct.

Expanded example:

Suppose someone accidentally writes:

```java
@Override
public boolean equal(Employee e)
```

instead of

```java
@Override
public boolean equals(Object o)
```

Compiler immediately reports:

"This method does not override or implement a method from a supertype."

Without @Override:

Java would simply create a completely new method:

```java
equal()
```

Object.equals() would remain unchanged.

This bug is difficult to notice.

---

# IDE-generated equals() and hashCode()

User asked:

"What do you mean by IDE-generated equals() and hashCode()? You said never write them manually."

Explanation:

In production, developers almost never write these methods manually.

Instead:

Generate

↓

equals()

↓

hashCode()

using the IDE.

Reason:

The generated implementation correctly handles:

- null checks
- type checks
- field comparisons
- consistent hash generation

Typical generated code:

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee)) return false;
    Employee employee = (Employee) o;
    return id == employee.id &&
           Objects.equals(name, employee.name);
}

@Override
public int hashCode() {
    return Objects.hash(id, name);
}
```

Interview answer:

"I understand how equals() and hashCode() work and can implement them manually if needed, but in production I usually allow the IDE to generate them to avoid subtle bugs."

---

# System.identityHashCode()

Mentioned as an optional topic.

Normal:

```java
obj.hashCode()
```

uses the overridden implementation.

System.identityHashCode():

```java
System.identityHashCode(obj)
```

ignores overridden hashCode() and returns an identity-based hash.

Example:

```java
Employee e1 = new Employee(1);
Employee e2 = new Employee(1);
```

If:

```text
e1.equals(e2)

↓

true
```

then:

```text
e1.hashCode()

==

e2.hashCode()
```

after proper overriding.

But:

```java
System.identityHashCode(e1)

System.identityHashCode(e2)
```

typically produce different values because they are different object instances.

Conclusion from discussion:

Interesting API but very rarely asked in SDE-2 interviews.

Not worth spending significant preparation time.

---

# Final Remaining Topics Check

Topics identified as still worth remembering:

- equals() contract
- hashCode() contract
- Objects.equals()
- Objects.hash()
- @Override
- IDE-generated equals()/hashCode()

Topics intentionally skipped for SDE-2:

- Deep hash algorithm implementation
- Exact String hash algorithm
- Historical HashMap implementation changes
- JVM identity hash implementation
- clone() internals
- finalize()
- Serialization interactions

---

# Final Assessment of Module

Coverage achieved:

- Object class
- equals()
- hashCode()
- toString()
- Comparable
- Comparator
- Immutable objects
- == vs equals()
- @Override
- HashMap lookup
- HashSet behavior
- Mutable keys
- String as HashMap key
- equals()/hashCode() contracts
- Defensive copying
- Records
- Object hierarchy
- IDE-generated equals()/hashCode()
- System.identityHashCode()

Conclusion:

This module was considered complete for SDE-2 Java backend interviews.

No meaningful interview topics related to this module remained before moving to the next Java topic.