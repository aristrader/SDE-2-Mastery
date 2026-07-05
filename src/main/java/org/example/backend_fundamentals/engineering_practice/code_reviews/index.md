---
order: 10
---

# Code Reviews

## Primary Purpose
A code review is a quality gate, not a coding competition. Its primary purpose is ensuring that the merged code is correct, maintainable, safe, and consistent with the existing codebase.

## What Should Be Reviewed?
1. **Correctness (Highest Priority):** Does it solve the ticket? Logical bugs? Race conditions? Missing edge cases? If correctness is wrong, everything else becomes secondary.
2. **Readability:** Can another engineer understand this code six months later? Look for naming, clear flow, and unnecessary complexity.
3. **Maintainability:** Will future engineers struggle to modify this? Look for huge methods, duplicate logic, and tight coupling.
4. **API Design:** Endpoint naming, request/response models, backward compatibility, idempotency.
5. **Performance:** N+1 queries, O(n²) algorithms, blocking operations. Do not micro-optimize unless it matters.
6. **Security:** Input validation, authorization, SQL injection, secrets in logs, proper error handling.
7. **Testing:** Are tests included? Happy path, edge cases, failure scenarios.

## Things Not Worth Commenting On
Avoid blocking PRs for personal stylistic preferences (variable names, formatting, different personal implementations). If both are readable and acceptable, let it go. Formatting should be handled by linters.

## When Should You Block a PR?
Legitimate reasons: incorrect logic, bugs, security vulnerabilities, breaking API compatibility, risk of data corruption, missing critical tests.

## Good vs Bad Review Comments
- **Bad:** "This code is bad." (No explanation) or "Rewrite this." (No actionable feedback).
- **Good:** "This query executes inside a loop, which could lead to N+1 database calls. Can we fetch all records in a single query instead?" (Specific, actionable, educational).

## Mindset as the PR Author
Instead of thinking "They're criticizing my code," think "They're improving the codebase." If disagreement exists, explain your reasoning, stay open to alternatives, and discuss synchronously if the thread gets long.

## Practical Tips
- Review intent, not just code ("Does this solve the business problem?").
- Think about future maintenance ("Can another engineer safely modify this later?").
- Think operationally (latency, migration risks, rollback).
- Be respectful; it's a technical discussion, not a judgment.
- Approving without requesting changes is perfectly acceptable if the code is genuinely good.

## Interview Perspective
**Question:** "What do you look for in a code review?"
**Answer:** "I prioritize correctness first—whether it solves the problem without logical issues. Then readability, maintainability, security, API compatibility, performance (like N+1 queries), and test coverage. I avoid commenting on purely stylistic preferences unless they violate conventions."

**Question:** "What if you disagree with a reviewer?"
**Answer:** "I'd explain my reasoning with technical justification. If we still disagree, I'd discuss it directly or involve the tech lead if it's an architectural decision. The goal is finding the best solution rather than winning the argument."

**Correction on Testing:** Tests passing does NOT guarantee correctness (missing test cases, incorrect business logic, null handling).
**Correction on API Design:** Even if architecture is finalized, code reviews still verify implementation details like REST endpoints and backward compatibility.

## Five Mental Questions During Every Code Review
1. Is it correct?
2. Can it break something? (Security, backward compatibility, edge cases)
3. Will it scale?
4. Can another engineer maintain it?
5. Are there sufficient tests?

## Quick recall

**Q. What is the highest priority in a code review?**
A. Correctness — ensuring the code solves the problem without logical bugs, race conditions, or missing edge cases.

**Q. When should you block a PR?**
A. For incorrect logic, security vulnerabilities, breaking API compatibility, or missing critical tests, but not for personal stylistic preferences.

**Q. What makes a good review comment?**
A. It should be specific, actionable, and educational, explaining the issue and suggesting an improvement.

**Q. Do passing tests guarantee correctness?**
A. No, tests could be missing edge cases, fail to check null handling, or the business logic itself could be flawed.

**Q. What are the five mental questions during every review?**
A. Is it correct? Can it break something? Will it scale? Can another engineer maintain it? Are there sufficient tests?
