---
order: 20
---

# Enums

## User understanding

User explained two practical production use cases.

---

## Use Case 1

Instead of:

```java
public static final String CREDIT = "CREDIT";

public static final String DEBIT = "DEBIT";
```

Use:

```java
enum TransactionType {

CREDIT,

DEBIT

}
```

Provides:

- grouping
- readability

---

## Use Case 2

Error Codes

Instead of maintaining:

- error code constants
- message constants
- mapping between them

Create one enum holding:

- code
- message

Very convenient.

---

## Review

Excellent understanding.

This is exactly how enums are commonly used.

---

## Additional Point

Java enums are full classes.

They may contain:

- fields
- constructors
- methods

Example:

```java
public enum ErrorCode {

USER_NOT_FOUND("E001","User not found"),

INVALID_TOKEN("E002","Invalid token");

private final String code;

private final String message;

ErrorCode(...)

...
}
```

---

## Another Example

Enums may even contain methods.

Example:

```java
public enum Status {

ACTIVE,

INACTIVE;

public boolean isActive(){

return this == ACTIVE;

}

}
```

---

## switch with enums

Example:

```java
switch(transactionType){

case CREDIT:

...

break;

}
```

Cleaner than comparing Strings.

---

## Type Safety

Instead of:

```java
process("credit");
```

Compiler enforces:

```java
process(TransactionType.CREDIT);
```

Typos eliminated.

---

## Final Revision

Enums:

- group related constants
- are type-safe
- improve readability
- can have:
  - fields
  - constructors
  - methods

Common backend usage:

- transaction status
- payment status
- order status
- roles
- error codes

---



<ExerciseNav />
