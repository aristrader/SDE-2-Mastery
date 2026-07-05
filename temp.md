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