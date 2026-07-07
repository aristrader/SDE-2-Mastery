---
order: 20
search: false
---

# Solutions

## Solution: nested-classes - Nested classes test

```java
public class InterviewQuestion {
    private final String text;
    
    public InterviewQuestion(String text) {
        this.text = text;
    }
    
    // Static nested class: Does NOT have access to enclosing instance's 'text'
    public static class AnswerKey {
        private final String answer;
        public AnswerKey(String answer) {
            this.answer = answer;
        }
        public String getAnswer() { return answer; }
    }
    
    // Inner class: HAS access to enclosing instance's 'text'
    public class Attempt {
        private final String submission;
        public Attempt(String submission) {
            this.submission = submission;
        }
        public void grade(AnswerKey key) {
            System.out.println("Q: " + text); // Accessing outer field
            System.out.println("Expected: " + key.getAnswer());
            System.out.println("Got: " + submission);
            System.out.println("Match? " + key.getAnswer().equals(submission));
        }
    }
    
    public static void main(String[] args) {
        InterviewQuestion q = new InterviewQuestion("What is 2 + 2?");
        
        // Created WITHOUT an outer instance reference
        AnswerKey key = new AnswerKey("4");
        
        // Created WITH an outer instance reference (q.new)
        Attempt attempt = q.new Attempt("4");
        
        attempt.grade(key);
    }
}
```
