---
order: 10
search: false
---

# Practice

## Exercise: nested-classes - Nested classes test

### Goal
Understand the difference in instantiation and scope between static nested classes and inner classes.

### Task
Create an `InterviewQuestion` outer class with:
- a static nested class `AnswerKey` that stores the expected answer
- an inner class `Attempt` that can access the outer question text
- a `main` method that creates both nested types correctly

### Starter code
```java
public class InterviewQuestion {
    private final String text;
    
    public InterviewQuestion(String text) {
        this.text = text;
    }
    
    // TODO: Create static nested class AnswerKey
    // TODO: Create inner class Attempt
    
    public static void main(String[] args) {
        // TODO: Create instance and print results
    }
}
```

### Checks
- `AnswerKey` must be created without an `InterviewQuestion` instance.
- `Attempt` must be created through an `InterviewQuestion` instance.
- Print the question text, expected answer, and submitted answer.
