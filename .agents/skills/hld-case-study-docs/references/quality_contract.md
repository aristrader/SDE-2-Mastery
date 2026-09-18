# QG-4 HLD Case-Study Acceptance Contract

Use this contract for every substantial HLD case-study rewrite. It is an acceptance gate, not a writing suggestion.
It protects public study material from a polished-looking but shallow rewrite.

## Required evidence before editing

Create a working evidence record for the run. It may be compact, but it must name the exact source section and final
destination for each item below.

| Evidence | Must prove |
| --- | --- |
| Source-preservation map | Every distinct existing claim, example, diagram, failure mode, estimate, and follow-up is retained, moved to a linked page, or explicitly cut with a reason. |
| Reference comparison | Compare against at least two accessible, independent interview-learning resources for scope, learning flow, and deep-dive coverage; use one authoritative source for each non-obvious factual mechanism. Record what each reference does better or confirms. No copied prose, structure, diagrams, or examples. |
| Reader-path map | Prompt and scope → naive baseline → concrete pressure → selected design → trade-off → failure/recovery → interview delivery. |
| Visual plan | Each visual has one reader question, medium, nearby explanation, and acceptance check at reading-column width. |

Do not edit a page until all four rows are concrete. “The page needs more detail” and “add a diagram” are not evidence.

## Reference-set quality baseline

The reference set is not judged by length or brand. Extract recurring observable strengths from accessible material:

- a clear problem restatement with must-have, deferred, and explicit non-goals;
- a time-aware interview path: requirements, scale, API/data model, high-level design, selected deep dive, and wrap-up;
- capacity arithmetic tied to a queue, storage, partitioning, provider limit, or latency decision;
- an end-to-end path with request/result states, not only a component diagram;
- one or two hard decisions explored through alternatives and trade-offs; and
- operational policy for retries, overload, correctness, observability, and user-visible outcome.

The final page must meet or exceed the set on causal clarity and first-reader usefulness. A reference may add product
features outside the chosen scope; record that as a conscious exclusion rather than inflating the case study.

## Required final page evidence

The completed case study must let a first-time reader answer all of these without unstated knowledge:

1. What is the product behavior and what is intentionally excluded?
2. What is the smallest workable baseline?
3. What concrete workload, correctness, or product pressure breaks that baseline?
4. What durable records exist, who owns them, and what is merely cache or best effort?
5. What happens on the normal write/event path, including the acceptance boundary?
6. What happens on the normal read/retrieval path, if the product has one?
7. Which alternative was rejected, what it optimizes, and why the chosen design is worth its cost?
8. What happens on timeout, duplicate delivery, dependency failure, and recovery—and what does the caller/user see?
9. Which terms are domain-critical and defined when first introduced?
10. How should a candidate explain the design aloud in interview order?

Use full causal prose for each deep dive: **problem → naive failure → mechanism → trade-off → recovery**. Tables,
headings, and diagrams support this explanation; they never replace it.

## Writer and critic passes

Use two separate passes after drafting.

### Writer pass

Trace the normal path and each selected failure path against the actual text, data model, and diagrams. Confirm that
every component has a pressure-driven reason and every guarantee names its boundary.

### Critic pass

Read the final page from the beginning as an unfamiliar interview learner. Reject it when any of these occur:

- a component or term appears before its purpose;
- a deep dive starts with the chosen technology rather than the pressure;
- a table or bullet list stands in for an explanation;
- a diagram duplicates prose or cannot answer its declared reader question quickly;
- a failure row says “retry” without deciding idempotency, expiry, ambiguity, or visible result;
- a useful source concept was shortened into a generic claim; or
- the page could not be used to give a coherent 35–40 minute interview answer.

Revise every rejection. Do not lower the standard to preserve a completion date.

## Rendered visual acceptance

At normal desktop reading-column width, inspect the real rendered page. Each visual must have no console errors or
overflow, readable labels, correct arrow direction, and a distinct job: ownership/context, normal flow,
failure/recovery, lifecycle, or strategy comparison. A technical visual that only inventories components fails.

## Completion record

Only after writer pass, critic pass, and rendered audit pass may the quality backlog mark the module
`Validated (QG-4)`. Record the page group, source packet, reference types consulted, validation commands, and any
intentional scope cuts. Until then use `Draft` or `Needs quality review`; never use `completed`.
