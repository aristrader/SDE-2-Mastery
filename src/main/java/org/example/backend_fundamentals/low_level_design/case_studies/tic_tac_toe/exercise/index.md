---
order: 10
search: false
---

# Final exercise — Tic-Tac-Toe LLD

## Exercise: tic-tac-toe-lld - Two-Player Board Game

### Goal

Design an in-memory two-player Tic-Tac-Toe game for a fixed 3×3 board.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Requirements

- A game has exactly two players, using `X` and `O`.
- `X` takes the first turn; players alternate after each successful move.
- A move supplies a row and column and marks one empty cell with the current player's symbol.
- Reject a move when coordinates are outside the board, the cell is occupied, it is the other player's
  turn, or the game has already finished.
- After each successful move, report whether that player completed a row, column, or diagonal.
- If every cell is occupied and no player won, report a draw.
- Expose the current board and game status for the runner to print.

### Constraints

- Keep the first implementation in memory and single-threaded.
- Do not add a UI, REST API, database, AI opponent, undo history, or online multiplayer.
- Keep runnable Java under `playground/` when implementation begins. Start from
  `playground/TicTacToeRun.java`.
- Design the fixed 3×3 version first. Do not generalize to N×N until the basic rules are correct.

### Test scenarios

- `X` wins one row.
- `O` wins one column.
- A player wins a diagonal.
- A move to an occupied cell is rejected and does not change the turn.
- A move after a win is rejected.
- Nine legal moves with no winner produce a draw.

### Interview follow-ups

- How would you support an N×N board and a configurable win length?
- What state would you add for undo/redo?
- How would an online version prevent two players from claiming the same cell?
- What changes if the system supports many games at once?
