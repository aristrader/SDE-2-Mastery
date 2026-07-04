# Nested Classes

> Understanding when and why to use nested classes in Java, focusing on the crucial difference between static and non-static nesting.

---

## The Two Main Types of Nested Classes

Java allows defining a class within another class. The most important interview concept is understanding the difference between **Non-static Inner Classes** and **Static Nested Classes**.

---

## 1. Non-static Inner Class

A non-static inner class is associated with an **instance** of its enclosing class.

```java
class Company {
    String companyName;

    class Employee {
        void print() {
            // Implicitly accesses the outer instance's field
            System.out.println(companyName); 
        }
    }
}
```

### Key Properties
- Every `Employee` implicitly stores a **hidden reference** to the specific `Company` object that created it.
- Because of this hidden reference, the inner class can freely access all instance members (even `private` ones) of the outer class.
- **Creation requires an outer instance:** You cannot instantiate the inner class without an outer instance.
  ```java
  Company c = new Company();
  Company.Employee e = c.new Employee(); // Notice the syntax!
  ```
  `new Company.Employee()` is illegal because it lacks an outer instance.

---

## 2. Static Nested Class

A static nested class acts just like a regular top-level class, but it is packaged inside another class for namespace/grouping purposes.

```java
class HttpClient {
    static class Builder {
        // ...
    }
}
```

### Key Properties
- A static nested class **does not** keep a reference to an outer instance.
- It **cannot** access instance variables or methods of the outer class (because there is no implicit `this`).
- **Creation does not require an outer instance:**
  ```java
  HttpClient.Builder builder = new HttpClient.Builder(); // Clean and simple
  ```

---

## Interview Rule of Thumb

**Why would a nested class ever be static?**
To avoid memory leaks and unnecessary overhead! Since a non-static inner class holds a hidden reference to the outer class, it keeps the outer class alive in memory (preventing Garbage Collection) as long as the inner class is alive.

**When to use which?**
Ask yourself: *Does the nested class need access to the instance variables of the outer object?*
- **If YES:** Use a non-static inner class.
- **If NO:** Use a static nested class. 

Examples of Static Nested Classes in the wild:
- `Map.Entry` inside the `Map` interface.
- Builder classes (e.g., `HttpClient.Builder`). The builder is used to create the object; it makes no sense to require an existing client object to create the builder!
