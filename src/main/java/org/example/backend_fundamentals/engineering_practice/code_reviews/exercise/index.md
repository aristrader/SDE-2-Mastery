---
order: 10
search: false
---

# Exercise

## Exercise: code-review-triage-and-blocking - PR Comment Triage and Blocking Decisions

### Problem Statement
You are conducting a code review for an SDE2 pull request adding a new payment retry worker in a distributed backend service. Evaluate the following four findings discovered during your review:

1. **Finding A:** The author used `StringBuilder` instead of `String.format()` for a single debug log message that runs once during application startup.
2. **Finding B:** Inside a `@Transactional` Kafka consumer method, the author makes a synchronous HTTP call to an external payment gateway with a 30-second timeout while holding a database row lock.
3. **Finding C:** A newly introduced REST endpoint returns snake_case JSON field names (`user_id`), whereas the company-wide API gateway contract mandates camelCase (`userId`).
4. **Finding D:** The unit tests assert HTTP 200 on happy paths, but there are no tests asserting behavior when the payment gateway returns HTTP 504 Gateway Timeout or HTTP 429 Too Many Requests.

### Requirements & Tasks
1. Categorize each finding into one of the following review actions:
   - **BLOCK:** Request changes with an explanation of technical risk.
   - **COMMENT (NON-BLOCKING):** Optional suggestion or educational note; does not block merging.
   - **DELEGATE TO TOOLING:** Should not be handled manually; recommend automation rule.
2. For each finding, provide a 1–2 sentence technical rationale explaining the risk or reason for your decision.

---

## Exercise: code-review-feedback-refactoring - Transforming Review Comments

### Problem Statement
A junior engineer left several vague or unconstructive comments on a peer's pull request. Transform each comment into a high-leverage, constructive, and actionable review comment suitable for a senior backend engineering environment.

1. **Original Comment 1:** "This method is too long and messy. Refactor it."
2. **Original Comment 2:** "Don't use `List<Order>` here. This will crash production."
3. **Original Comment 3:** "Change variable name `d` to `durationInSeconds`."

### Requirements & Tasks
Rewrite each comment adhering to the constructive review principles:
- State the specific technical issue or risk clearly.
- Explain *why* it matters (performance, maintainability, or bug).
- Suggest a concrete, actionable alternative or path forward.
- Distinguish between blocking issues and optional preferences.
