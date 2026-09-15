---
order: 150
---

# Tic-Tac-Toe LLD

Java warm-up for board state, turn validation, and winner detection.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed
[final exercise](exercise/). Record entity identification and the class diagram in
[playground](playground/entity_identification_and_class_diagrams.md). Keep the first pass in memory and
single-game only; board generalization, a UI, persistence, and online multiplayer are follow-ups.

## Quick recall

- A move is valid only when the game is active, coordinates are in bounds, and the cell is empty.
- Evaluate a win after a successful move, not after an invalid attempt.
- Draw is a terminal state when the board fills without a winner.
