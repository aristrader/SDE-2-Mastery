# Design Documents and RFCs

## Why Design Documents Exist
- Align engineers before implementation.
- Catch architectural mistakes early.
- Record architectural decisions.
- Get engineering feedback before implementation begins.
Core idea: Design first. Code later.

## When to Write One
**Write a Design Doc:**
- New service
- Major architectural change
- Cross-team APIs
- Database redesign
- Infrastructure changes

**Don't Write One:**
- Bug fixes
- Small CRUD features
- Minor refactoring
- Simple endpoints

## Standard Design Document Structure
Typical sections (order is not strict):
1. **Context / Background**: Why are we discussing this? Avoid jumping straight into implementation.
2. **Problem Statement**: State the problem clearly (e.g., "Existing notification service cannot support multiple channels and cannot scale beyond 5k notifications/sec.").
3. **Goals**: Measurable objectives (e.g., Support Email, SMS, Push, handle 100k notifications/sec, retry failed deliveries).
4. **Non-Goals**: Explicitly state what will NOT be solved to prevent scope creep (e.g., Notification editor).
5. **Requirements & Constraints**: Functional, non-functional (Latency, Availability), and Constraints (Existing Kafka cluster must be used).
6. **Proposed Design**: Main section containing architecture, APIs, database, components, message flow, sequence diagrams.
7. **Alternatives Considered**: Crucial for demonstrating engineering judgment. Compare options with pros and cons.
8. **Tradeoffs**: Every design has tradeoffs. Explain *why* a technology was chosen over another (e.g., "We chose Kafka because throughput is more important than simplicity.").
9. **Risks**: Shows awareness beyond the happy path (e.g., single point of failure, provider rate limits).
10. **Rollout Plan**: Safe deployment strategy (feature flags, canary, rollback).
11. **Monitoring**: How success will be measured (latency, throughput, alerts). A design without observability is incomplete.
12. **Open Questions**: Things intentionally left unresolved.

### Six Questions Every Good Design Proposal Answers
1. Why are we building this?
2. What are we building?
3. What are we NOT building?
4. How will we build it?
5. Why this approach instead of others?
6. How will we safely deploy and operate it?

## RFC (Request For Comments)

### Definition
An RFC is a proposal shared before implementation to gather engineering feedback ("Here's my proposed solution. Please review it before I build it."). Often, an RFC is simply a design document opened for discussion.

### Typical RFC Lifecycle
Problem identified → Author writes RFC → Share with stakeholders → Comments & discussion → Revise RFC → Approval → Implementation.

### Reviewers and Feedback
**Who Reviews:** Depends on impact. Small services involve team members and Tech Lead. Large changes involve multiple teams, Architects, Security, SRE, etc.
**Good Feedback:** Why Kafka? Scalability concerns? Backward compatibility? Security concerns?
**Bad Feedback:** Rename heading, font size, grammar. Purpose is engineering discussion.

### Possible Outcomes
Outcomes include Approved, Approved with changes, Needs more investigation, Rejected. Changing the proposal after feedback is expected. Don't become emotionally attached to your design. Optimize for product quality rather than personal ownership.

## Interview Perspective
Interviewers don't expect memorization of sections. They expect structured thinking.
**Takeaway:** Do NOT immediately jump into technologies. Clarify requirements, constraints, and scale first.
**Common Question:** "How do you handle disagreements on architecture?"
**Strong Answer:** Document proposal → Discuss tradeoffs → Collect stakeholder feedback → Revise design → Proceed after agreement.

## Quick recall

**Q. When should you write a design doc?**
A. For new services, major architectural changes, cross-team APIs, or database redesigns. Not for bug fixes or simple CRUD features.

**Q. Why include a Non-Goals section?**
A. To explicitly state what will not be solved, preventing scope creep.

**Q. What is the value of the Alternatives Considered section?**
A. It demonstrates engineering judgment by showing you evaluated multiple options and understand the tradeoffs.

**Q. What makes a good interview answer when asked to design a system?**
A. Clarify requirements, constraints, and scale first before jumping into specific technologies like Kafka or Redis.

**Q. What is the goal of an RFC?**
A. To gather engineering feedback early on architecture, risks, and tradeoffs before implementation begins.

**Q. How do you handle architectural disagreements?**
A. Document the proposal, discuss tradeoffs, collect stakeholder feedback, revise the design, and proceed after agreement.
