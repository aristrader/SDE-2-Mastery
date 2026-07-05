---
order: 40
---

# Normalization & Denormalization

Normalization organizes data into multiple related tables to reduce redundancy, improve consistency/integrity, and avoid update anomalies. Denormalization deliberately re-introduces redundancy to speed up reads. Senior interviews want the trade-off and the ability to walk a table through the normal forms.

## Why normalize — the update anomaly

Store a customer's email once, not on every order row. If it's duplicated across order rows and Alice changes her email, an `UPDATE` that misses one row leaves the database inconsistent:

```text
OrderID  Email
1        new@gmail.com
2        alice@gmail.com   ← missed → inconsistent
```

Normalized: `Customers(CustomerID, Name, Email)` + `Orders(OrderID, CustomerID, Product)` — the email lives in exactly one place, so one update fixes everything.

## Functional dependencies (the vocabulary normal forms are defined in)

`A → B` means knowing A uniquely determines B. `StudentID → Name` holds (one ID, one name); `Name → StudentID` does not (two students can share a name).

- **Full functional dependency:** a non-key column depends on the *entire* composite key. `(StudentID, CourseID) → Grade` — you need both.
- **Partial dependency:** a non-key column depends on only *part* of a composite key. With PK `(StudentID, CourseID)`, `StudentID → StudentName` is partial (CourseID is irrelevant). **Violates 2NF.**
- **Transitive dependency:** a non-key column depends on another non-key column. `EmployeeID → DepartmentID → DepartmentName`, so `EmployeeID → DepartmentName` indirectly. **Violates 3NF.**

## The normal forms

| Form | Rule (one line) |
|------|-----------------|
| **1NF** | Atomic values only — no lists/arrays in a cell |
| **2NF** | 1NF + no partial dependencies (every non-key column depends on the *whole* key) |
| **3NF** | 2NF + no transitive dependencies (non-key columns depend only on the key) |
| **BCNF** | 3NF + every *determinant* is a candidate key |

2NF only becomes meaningful when the primary key is **composite** — with a single-column key there's no "part of the key" to depend on partially.

## Worked journey — dirty table to BCNF

**Dirty table** — repeating groups, partial + transitive dependencies, heavy redundancy:

```text
StudentID StudentName DepartmentID DepartmentName CourseID CourseName InstructorID InstructorName Phones
S1        John        D1           CS             C1       DBMS      I1           Alice          111,222
```

**→ 1NF** — `Phones` holds multiple values. Split phones into their own atomic rows:

```text
StudentPhones(StudentID, Phone):  (S1,111), (S1,222), (S2,333), ...
```

**→ 2NF** — PK is `(StudentID, CourseID)`, but `StudentID → StudentName, DepartmentID` and `CourseID → CourseName, InstructorID` depend on only part of the key. Split:

```text
Students(StudentID, StudentName, DepartmentID)
Courses(CourseID, CourseName, InstructorID)
Enrollment(StudentID, CourseID)
```

**→ 3NF** — transitive dependencies remain: `DepartmentID → DepartmentName` and `InstructorID → InstructorName`. Extract them:

```text
Departments(DepartmentID, DepartmentName)
Instructors(InstructorID, InstructorName)
```

**→ BCNF** — classic case: table `(Student, Course, Instructor)` with `Course → Instructor` and candidate key `(Student, Course)`. The determinant `Course` is not a candidate key → BCNF violation. Split:

```text
CourseInstructor(Course, Instructor)
Enrollment(Student, Course)
```

So 3NF can still leave an anomaly when a non-key column determines part of a candidate key — BCNF closes that gap.

## Denormalization

Intentional redundancy to avoid joins. Instead of joining Users + Orders + Products + Payments for a dashboard, store a pre-joined `OrderSummary(OrderID, UserName, ProductName, …)` and `SELECT * FROM OrderSummary` — no joins, much faster reads.

**Cost:** more storage, and updates must touch every copy. If Alice renames herself, you update `Users`, `OrderSummary`, the analytics table, the cache — miss one and you're inconsistent. You trade write complexity for read speed.

**Where it's used in practice:**
- **E-commerce order snapshots** — store product name + price + user name *at order time* (also the correct behavior: the order should reflect the price when purchased, not today's price).
- **Analytics / OLAP** — joins over huge tables are expensive, so schemas are denormalized (star/snowflake).
- **Social feeds** — feed rows already carry `author_name`, `author_avatar`, `like_count` instead of joining per request.

## Normalize vs denormalize — the trade-off table

| Normalization | Denormalization |
|---------------|-----------------|
| Reduces redundancy | Introduces redundancy |
| Better consistency / integrity | Better read performance |
| More joins | Fewer joins |
| Smaller storage | Larger storage |
| Easier (single-point) updates | Harder (multi-point) updates |
| Common in OLTP | Common in read-heavy / OLAP systems |

**Interview framing:** normalize for correctness by default (OLTP); denormalize deliberately, for measured read hot-paths, accepting you now own keeping the copies in sync.

## Quick recall

**Q. 1NF / 2NF / 3NF / BCNF in one line each?**
A. 1NF = atomic values; 2NF = no partial dependency; 3NF = no transitive dependency; BCNF = every determinant is a candidate key.

**Q. Partial vs transitive dependency?**
A. Partial = non-key column depends on part of a composite key (breaks 2NF). Transitive = non-key column depends on another non-key column (breaks 3NF).

**Q. When does 2NF even apply?**
A. Only with a composite primary key — there's no "part of the key" to depend on otherwise.

**Q. What does BCNF fix that 3NF misses?**
A. A non-key (or partial-key) column that determines part of a candidate key — BCNF requires every determinant to be a candidate key.

**Q. Why denormalize, and what's the cost?**
A. Avoid expensive joins on read-heavy paths; cost is redundant storage and multi-place updates that risk inconsistency.

**Q. What's the update anomaly?**
A. Duplicated data updated in some rows but not all, leaving conflicting values — the core problem normalization removes.
