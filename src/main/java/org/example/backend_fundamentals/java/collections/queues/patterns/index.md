---
order: 40
---

# Queue Patterns

Queues and deques become useful when the problem has a real processing rule: first-in-first-out, two-stack navigation, priority ordering, or both-end comparison.

## Common patterns

| Pattern | Structure |
| --- | --- |
| Service line / BFS | `Queue` |
| Back/forward navigation | two `Deque` stacks |
| Palindrome / both-end comparison | `Deque` |
| Priority scheduling | `PriorityQueue` |

## Implementation choice

- Use `Queue` as the variable type when the code only needs FIFO operations.
- Use `Deque` when both ends matter or when replacing `Stack`.
- Use `PriorityQueue` when removal order comes from natural ordering or a comparator.
- Use `ArrayDeque` as the implementation for normal queue/deque work.

## Browser history model

Think of browser history as three pieces of state:

- back history;
- current page;
- forward history.

When going back, push the current page to forward history, pop the previous page from back history, then make that page current.

When going forward, push the current page to back history, pop the next page from forward history, then make that page current.

When visiting a new page, push the current page to back history, clear forward history, then make the new page current.

## Quick recall

- **Browser history?** Back history + current page + forward history.
- **FIFO simulation?** Queue.
- **Highest priority first?** PriorityQueue with comparator.
- **Why interface variable type?** It limits code to the behavior it actually needs.
