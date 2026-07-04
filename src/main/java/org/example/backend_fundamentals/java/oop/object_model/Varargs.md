# Variable Arguments (Varargs)

## User understanding

Initially confused.

Thought perhaps related to var.

Then guessed:

Variable number of arguments.

---

## Review

Correct.

---

Example:

Instead of writing:

```java
void print(int a)

void print(int a,int b)

void print(int a,int b,int c)
```

Use:

```java
void print(int... nums)
```

Allows:

```java
print()

print(1)

print(1,2)

print(1,2,3)

print(1,2,3,4,5)
```

---

## Internal Behavior

Compiler treats:

```java
int...
```

like

```java
int[]
```

Inside method it behaves exactly like an array.

---

## Rules

Only one varargs parameter.

---

Must be the last parameter.

Valid:

```java
foo(String name,int... nums)
```

Invalid:

```java
foo(int... nums,String name)
```

---

## Real-world Examples

String.format(...)

System.out.printf(...)

---

## Final Revision

Know:

- variable number of arguments
- internally an array
- one varargs only
- must be last parameter

---

