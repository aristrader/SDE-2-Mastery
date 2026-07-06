---
order: 40
---

# Technical Writing

## Purpose
Technical writing is about communicating technical ideas clearly, not about English proficiency.
**Core rule:** Optimize for the next engineer, not for sounding smart. Prioritize clarity over cleverness.

## Common Places for Technical Writing
- PR descriptions
- Design docs
- Jira tickets
- README
- API documentation
- Incident postmortems
- Slack messages

## Good vs Bad Writing
- **Bad:** "Fixed issue." (Unclear which issue, why, or how).
- **Good:** "Fixed duplicate notifications caused by concurrent retries. Added idempotency check before sending notifications and included regression tests." (Communicates the problem, cause, and solution).

## Comments in Code
Don't explain what the code obviously does (e.g., `// Increment i`). Instead, explain *why* something exists (e.g., `// Retry only for transient failures to avoid duplicate notifications.`).

## PR Description
A good PR description allows someone to understand the purpose without reading every file. It should answer:
- What changed?
- Why?
- Migration required?
- Rollback concerns?
- How was it tested?

## Design Documents and Incident Updates
- **Design Docs:** Avoid unnecessary buzzwords ("Leveraging a highly optimized distributed asynchronous architecture"). Be clear ("Messages are published to Kafka, and workers consume them asynchronously to improve throughput").
- **Incident Updates:** Provide useful status updates. Instead of "Looking into it", use "High database latency observed after deployment. We're investigating slow queries. Rollback is ready if latency continues to increase."

## Interview Relevance
Direct questions are uncommon, but interviewers evaluate technical writing indirectly through system design explanations, communication clarity, and structured thinking.

## Takeaway
Write for someone who has never seen your code before. The goal is to reduce the number of questions future engineers need to ask.

## Quick recall

**Q. What is the core rule of technical writing?**
A. Optimize for the next engineer by prioritizing clarity over sounding smart.

**Q. What makes a good PR description?**
A. It answers what changed, why it changed, migration/rollback concerns, and how it was tested.

**Q. How should code comments be written?**
A. Explain *why* a piece of code exists, rather than stating what the code obviously does.

**Q. How should incident updates be communicated?**
A. Provide actionable context on the symptoms, current investigation focus, and potential mitigation steps, rather than just saying "Looking into it."


