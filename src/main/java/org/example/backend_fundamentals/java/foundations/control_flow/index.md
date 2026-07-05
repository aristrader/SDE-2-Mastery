---
order: 10
---

# Control Flow

## User understanding

Covered:

- if
- else if
- else
- switch
- break
- default
- for
- while
- enhanced for loop

Mentioned enhanced loop syntax:

for (Type obj : list)

---

## Review

Correct.

---

## Enhanced For Loop

Example:

```java
for (Employee employee : employees) {

}
```

Equivalent to index-based loop when index is not required.

---

## Important Interview Point

Cannot modify collection while iterating using enhanced for.

Example:

```java
for (Integer num : list) {

    list.remove(num);

}
```

Throws:

ConcurrentModificationException

Need:

Iterator

(Collections topic later.)

---

## Modern Switch

Traditional:

```java
switch(day){

case MONDAY:
break;

default:

}
```

Modern Java:

```java
switch(day){

case MONDAY -> work();

case SATURDAY, SUNDAY -> rest();

}
```

Also supports switch expressions.

Need only awareness.

---

## Final Revision

Know:

- for
- enhanced for
- switch
- modern switch syntax
- don't modify collections during enhanced for iteration.

---



<ExerciseNav />
