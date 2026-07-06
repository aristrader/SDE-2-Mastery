---
order: 20
search: false
---

# Solution

One possible structure:

```java
public class InterviewQuestion {
  private final String text;

  public InterviewQuestion(String text) {
    this.text = text;
  }

  static class AnswerKey {
    private final String expected;

    AnswerKey(String expected) {
      this.expected = expected;
    }
  }

  class Attempt {
    private final String submitted;

    Attempt(String submitted) {
      this.submitted = submitted;
    }

    void print(AnswerKey key) {
      System.out.println("Question: " + text);
      System.out.println("Expected: " + key.expected);
      System.out.println("Submitted: " + submitted);
    }
  }

  public static void main(String[] args) {
    AnswerKey key = new AnswerKey("Use outer.new Inner() for inner classes");

    InterviewQuestion question = new InterviewQuestion("How do you create an inner class?");
    Attempt attempt = question.new Attempt("Create outer first, then outer.new Attempt()");

    attempt.print(key);
  }
}
```

`AnswerKey` is static, so it is created as `new AnswerKey(...)` from inside the outer class. `Attempt` is an inner class, so it is created as `question.new Attempt(...)`.
