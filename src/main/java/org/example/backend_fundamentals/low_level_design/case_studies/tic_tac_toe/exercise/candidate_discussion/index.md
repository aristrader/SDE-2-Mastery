---
search: false
---

# Candidate discussion — Tic-Tac-Toe

Start by proposing a small first scope:

> “I’ll implement an in-memory 3×3 game for two players, where three consecutive marks win. I’ll keep
> board state and winner evaluation isolated so board size and win rules can be extended later if needed.”

Then confirm the assumptions that change the model or the core flow.

## Questions to ask

1. Is the first version a standard 3×3 board with `X` and `O`, or should it support configurable board
   size and win length now?
2. Does `X` always start, and do players alternate only after a successful move?
3. Should an occupied cell, out-of-bounds coordinate, wrong turn, or move after game completion be
   rejected?
4. Should the game report a win immediately after each move and a draw when the board fills?
5. Is this a single in-memory game, or are UI, persistence, online multiplayer, undo, and multiple games
   in scope?

## Agreed scope for this exercise

- Fixed 3×3 board, exactly two players, `X` starts, and three consecutive marks win.
- One in-memory, single-threaded game.
- Invalid moves are rejected without changing turn or board state.
- UI, REST APIs, persistence, AI, undo/redo, online play, and N×N generalization are follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
