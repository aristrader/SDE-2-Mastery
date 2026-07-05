---
order: 70
---

# Java Interview Notes Dump - Object Class Module (Part 1)

## Module Overview

Topics covered:

- Object class
- equals()
- hashCode()
- toString()
- Comparable
- Comparator
- Immutable objects
- HashMap lookup
- HashSet behavior
- String as HashMap key
- Mutable keys in HashMap
- equals() / hashCode() contracts
- Object hierarchy
- @Override
- IDE-generated equals()/hashCode()
- System.identityHashCode()

Goal:
- SDE-2 Java backend interview preparation.
- Focus on high-ROI interview knowledge.
- Connect concepts with Collections, Spring Boot, production code, and common interview questions.

---

# Study Flow

The agreed learning process for each topic:

1. Explain the topic in your own words.
2. Review correctness.
3. Fill interview-important gaps.
4. Cover edge cases and interview follow-ups.
5. Move to the next topic only after understanding.

---

# Object Class

## User Understanding

- Every class in Java automatically implements the Object class.
- Object contains methods like:
  - equals()
  - hashCode()
  - toString()
- These methods can be overridden with custom implementations.

### Misconception

> Every class automatically implements Object.

### Correction

Every class automatically **extends** Object, either directly or indirectly.

Example:

```java
class Employee {
}
```

is equivalent to

```java
class Employee extends Object {
}
```

unless another class is already being extended.

Object is the root of Java's class hierarchy.

---

## Important Object Methods

Methods inherited from Object discussed:

- equals()
- hashCode()
- toString()
- getClass()
- clone() (protected)
- wait()
- notify()
- notifyAll()
- finalize() (deprecated)

Interview focus is primarily:

- equals()
- hashCode()
- toString()
- getClass()

---

# Why Object Exists

## User Thought

The language was designed this way and it helps with inheritance and generics.

### Correction / Expanded Explanation

Yes, Object exists because of Java's language design.

The important interview explanation is:

Without Object:

```
Dog

Car

Employee

Student
```

would all be unrelated.

Java would not be able to write methods like:

```java
void print(Object obj)
```

because there would be no common parent.

With Object:

```
Object
│
├── Dog
├── Car
├── Employee
└── Student
```

Everything becomes polymorphic.

This enables:

- Generic APIs
- Collections
- Reflection
- Serialization
- println(Object)
- Objects.equals()

---

# equals()

## User Understanding

Default equals() compares object references.

Example:

```java
Employee e1 = new Employee("John");
Employee e2 = new Employee("John");
```

Although values are identical:

```
e1.equals(e2)
```

returns false because the references are different.

Therefore custom classes generally override equals().

### Review

Correct.

Default Object.equals() performs reference equality.

---

# hashCode()

## User Understanding

- hashCode is generated for every object.
- You can define your own hashCode implementation.
- It should be based on the values stored inside the object.

---

### Misconception

> hashCode is generated per object.

### Correction

A hashCode is simply an integer used for hashing.

The default implementation usually uses object identity.

When overriding hashCode(), the programmer chooses which fields determine the hash.

Example:

```java
@Override
public int hashCode() {
    return Objects.hash(id, name);
}
```

Two logically equal objects should produce the same hash.

---

# equals() and hashCode() Relationship

## User Understanding

The first thing checked is hashCode.

If hashCode matches, then equals() is used.

### Partial Correction

This is true **inside hash-based collections** such as:

- HashMap
- HashSet
- Hashtable

It is NOT how equals() itself works.

Calling

```java
a.equals(b)
```

does not compare hash codes first.

It immediately executes the equals() implementation.

---

# Hash Collisions

## User Understanding

If two different objects generate the same hashCode then a collision occurs.

A good hash function tries to minimize collisions.

### Review

Correct.

Different objects are allowed to have identical hash codes.

Example:

```
Object A -> hashCode = 42

Object B -> hashCode = 42
```

This is legal.

equals() is then used to distinguish them.

---

# HashMap Lookup Process

## User Understanding

HashMap stores objects into buckets.

If several objects end up in the same bucket then HashMap loops through them and checks equals().

### Expanded Lookup Process

Actual lookup:

```
key.hashCode()

↓

Bucket index calculated

↓

Go to bucket

↓

Compare stored hashes

↓

If hashes match

↓

equals()

↓

Found
```

HashMap relies on BOTH:

- hashCode()
- equals()

---

# Comparable

## User Understanding

Comparable is an interface.

A class can implement Comparable.

It provides methods for comparison.

---

### Expanded Explanation

Comparable defines a class's **natural ordering**.

Example:

```java
class Employee implements Comparable<Employee>
```

Implement:

```java
compareTo(Employee e)
```

Example:

```java
return this.salary - e.salary;
```

Now

```java
Collections.sort(list);
```

works automatically.

Only one natural ordering exists.

The class itself decides how it should normally be sorted.

---

# Comparator

Initially only briefly discussed by the user.

Expanded explanation:

Suppose Employee contains:

```java
int id;
String name;
int salary;
```

Possible sorting choices:

- id
- salary
- name
- joining date

Comparable only supports one natural ordering.

Comparator supports unlimited custom orderings.

Example:

```java
Comparator<Employee> salaryComparator =
    (a,b) -> a.salary - b.salary;
```

or

```java
Comparator<Employee> nameComparator =
    Comparator.comparing(Employee::getName);
```

Sorting:

```java
Collections.sort(list, salaryComparator);
```

Tomorrow:

```java
Collections.sort(list, nameComparator);
```

No modification to Employee required.

---

# Comparable vs Comparator

Interview comparison:

| Comparable | Comparator |
|------------|------------|
| Inside class | Separate class/object |
| compareTo() | compare() |
| One natural ordering | Unlimited custom orderings |
| Collections.sort(list) | Collections.sort(list, comparator) |

Conceptual explanation:

Comparable:

"I know how I should be sorted."

Comparator:

"I know how I want to sort you."

---

# Immutable Objects

## User Understanding

To make a class immutable:

- Make class final.
- Then objects automatically become immutable.

### Misconception

Making a class final automatically makes objects immutable.

### Correction

False.

final class only prevents inheritance.

Example:

```java
final class Employee {

    int age;

}
```

Still legal:

```java
Employee e = new Employee();

e.age = 50;
```

Object is mutable.

True immutability requires:

- final class
- private final fields
- initialize through constructor
- no setters
- defensive copying when mutable fields exist

Example:

```java
final class Employee {

    private final int id;

    Employee(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

}
```

---

# Records

## User Understanding

Records are lightweight DTO-like classes.

They are immutable.

### Expanded Explanation

Records automatically generate:

- constructor
- equals()
- hashCode()
- toString()

Common usage:

- DTOs
- Request objects
- Response objects
- Value objects

---

# Interview Rule

Whenever equals() is overridden:

hashCode() must also be overridden.

Reason:

Hash-based collections depend on both methods remaining consistent.

Failure causes HashSet and HashMap to behave incorrectly.

---

# Common Interview Questions Mentioned

Topics expected after this module:

- Why override equals() and hashCode() together?
- Can unequal objects have same hashCode?
- Can equal objects have different hashCode?
- Why mutable objects are bad HashMap keys?
- Why String is a good HashMap key?
- Why HashSet needs both hashCode and equals?
- Comparable vs Comparator
- Designing immutable classes
- Methods inherited from Object

```

**I have completed Part 1. This dump will require approximately 2 parts. Reply "Continue" to proceed to the next part.**

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

# Additional Notes for Object Class Module

These are the small but important points that were discussed (or implied) during the conversation but were not explicitly captured in the original dump.

---

# 1. `toString()`

## Purpose

`toString()` returns a string representation of an object.

Default implementation:

```text
ClassName@HexHash
```

Example:

```java
Employee e = new Employee(1, "John");

System.out.println(e);
```

Without overriding:

```text
Employee@3d075dc0
```

After overriding:

```text
Employee{id=1, name='John'}
```

## Production Uses

- Logging
- Debugging
- Printing objects
- API debugging

---

# 2. Misconception: "hashCode() is always checked before equals()"

## Misconception

> hashCode() is always checked before equals().

## Correction

This is **only true inside hash-based collections** like:

- HashMap
- HashSet
- Hashtable

Calling

```java
a.equals(b);
```

does **not** call `hashCode()` first.

It directly executes your `equals()` implementation.

---

# 3. Exact HashMap Lookup Sequence

Memorize this flow for interviews.

```text
Key
    │
    ▼
hashCode()
    │
    ▼
Bucket Index Calculation
    │
    ▼
Go to Bucket
    │
    ▼
Compare Stored Hash
    │
    ▼
equals()
    │
    ▼
Return Value
```

HashMap therefore depends on **both**:

- `hashCode()`
- `equals()`

---

# 4. Natural Ordering vs Custom Ordering

A simple interview-friendly explanation.

## Comparable

The object decides how it should normally be sorted.

Example:

Employee naturally sorts by `id`.

## Comparator

The caller decides how the object should be sorted.

Example:

Today sort by salary.

Tomorrow sort by name.

Next week sort by joining date.

---

# 5. Production Practice

Although every Java developer should understand how `equals()` and `hashCode()` work internally, they are usually **generated by the IDE** in production code.

A good interview answer:

> "I understand how `equals()` and `hashCode()` work and can implement them manually if required. However, in production I usually let the IDE generate them to avoid subtle bugs and ensure both methods remain consistent."

---

# 6. Interview Rule

Whenever you override:

```java
equals()
```

Always override:

```java
hashCode()
```

Otherwise:

- HashSet behaves incorrectly.
- HashMap lookup may fail.
- `contains()` may return false unexpectedly.
- `remove()` may fail even though the object exists.

---

# 7. Interview Rule (Very Important)

If

```text
a.equals(b) == true
```

then

```text
a.hashCode() == b.hashCode()
```

**must** be true.

However,

```text
a.hashCode() == b.hashCode()
```

does **not** imply

```text
a.equals(b)
```

because **hash collisions are allowed**.

Remember:

```text
Equal Objects
        │
        ▼
Must Have Same HashCode
```

```text
Unequal Objects
        │
        ▼
May Have Same HashCode
```

---

# 8. Misconception: `final` Class Means Immutable

## Misconception

> If a class is `final`, then its objects automatically become immutable.

## Correction

`final` only prevents inheritance.

It does **not** prevent object state from changing.

Example:

```java
final class Employee {

    int age;

}
```

Still legal:

```java
Employee e = new Employee();

e.age = 50;
```

The object is still mutable.

A truly immutable class generally requires:

- `final` class
- `private` fields
- `final` fields
- Constructor initialization
- No setters
- Defensive copying for mutable fields

---

# 9. Misconception: Every Class Implements Object

## Misconception

> Every class implements Object.

## Correction

Every class **extends** `Object` (directly or indirectly).

Interfaces are implemented.

Classes are extended.

Example:

```java
class Employee {
}
```

is equivalent to

```java
class Employee extends Object {
}
```

---

# 10. Terminology Correction

## User Wording

> "No one can implement it."

## Correct Terminology

A class is **extended**.

An interface is **implemented**.

Correct wording:

> "Making a class `final` prevents other classes from extending it."

---

# 11. Small Interview Tip

When asked:

> "Why doesn't HashMap simply use `equals()`?"

Answer:

Without hashing, HashMap would have to compare the key against every stored key using `equals()`.

For millions of entries, lookup becomes **O(n)**.

Using `hashCode()` first narrows the search to a single bucket.

Only then is `equals()` used.

Average lookup therefore becomes approximately **O(1)**.

---

# 12. Small Interview Tip

A good mental model:

```text
Comparable
        │
        ▼
"I know how I should be sorted."
```

```text
Comparator
        │
        ▼
"I know how I want to sort you."
```

This explanation is concise and interview-friendly.

---

# 13. Small Interview Tip

A good mental model for equality:

```text
==
```

Questions:

> "Are these the exact same object?"

---

```text
equals()
```

Questions:

> "Do these objects represent the same logical value?"

---

# Final Revision Checklist

- Object class
- Object hierarchy
- Methods inherited from Object
- `equals()`
- `hashCode()`
- `toString()`
- `==` vs `equals()`
- Comparable
- Comparator
- Hash collisions
- HashMap lookup process
- HashSet behavior
- `equals()`/`hashCode()` contracts
- Mutable keys in HashMap
- Why `String` is a good HashMap key
- Immutable classes
- Defensive copying
- Records
- `@Override`
- IDE-generated `equals()`/`hashCode()`
- `System.identityHashCode()` (optional)

<ExerciseNav />
