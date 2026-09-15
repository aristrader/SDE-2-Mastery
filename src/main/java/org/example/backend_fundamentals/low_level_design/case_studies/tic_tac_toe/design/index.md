---
order: 20
search: false
---

# Tic-Tac-Toe Design Notes

Use the playground note to identify entities and draw the initial class diagram before coding.

## Rules to preserve

- The current player can place exactly one symbol in one empty, in-bounds cell.
- A valid move changes the board and then passes the turn, unless it ends the game.
- A winner or draw is terminal; later moves cannot change the board.
- Win detection checks the row, column, and diagonals affected by the valid move.

## Decisions to make

- Which type owns board mutation and bounds/occupied-cell validation?
- Where should current turn and terminal status live?
- Should the runner pass a `Player`, symbol, or player ID to a move?
- How will the board expose a printable state without leaking mutable storage?

## Quick recall

- Start with fixed dimensions and direct win checks; generalization is a follow-up.
- Keep move validation and state transition together so an invalid move cannot change the turn.
