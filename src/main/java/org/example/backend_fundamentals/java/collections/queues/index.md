---
order: 100
---

# Queues and Deques

Queues model "who is next?" Deques model "which end do I add/remove from?" Read this folder after Lists and Sets.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `basics` | FIFO queue, empty-method behavior, `ArrayDeque` default |
| 2 | `deque` | Two-ended operations, stack replacement, palindrome checks |
| 3 | `patterns` | Simulations, browser history, implementation choice |

## Main implementations

| Type | Use when |
| --- | --- |
| `ArrayDeque` | Default FIFO queue or stack-like deque. |
| `PriorityQueue` | Remove by priority, not insertion order. |
| `LinkedList` | Rarely best; only when you specifically need list + deque behavior. |

## Quick recall

- **FIFO queue default?** `ArrayDeque`.
- **Stack replacement?** `ArrayDeque` through `Deque`.
- **Priority-based removal?** `PriorityQueue`.
- **Does `PriorityQueue` preserve insertion order?** No.
