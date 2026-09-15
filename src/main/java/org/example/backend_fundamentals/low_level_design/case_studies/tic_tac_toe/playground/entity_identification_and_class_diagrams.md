# Tic-Tac-Toe: Entity Identification and Class Diagram

## Final problem statement

Design an in-memory two-player Tic-Tac-Toe game for a fixed 3×3 board.

### Interview follow-ups

- How would you support an N×N board and a configurable win length?
- What state would you add for undo/redo?
- How would an online version prevent two players from claiming the same cell?
- What changes if the system supports many games at once?

### Requirements

- A game has exactly 2 players, using symbols X and O.
- X takes the first turn, and turns alternate after every successful move.
- A player can place their symbol only on an empty cell within the board boundaries.
- Invalid moves should be rejected, including:
  - playing out of turn
  - coordinates outside the board
  - selecting an already occupied cell
  - making a move after the game has finished
- After every successful move, determine whether the current player has won.
- If all cells are occupied and there is no winner, the game ends in a draw.
- The current game status and board state should be retrievable at any time.

### Constraints

- Keep the first implementation in memory and single-threaded.
- Design the fixed 3×3 version first. Do not generalize to N×N until the basic rules are correct.

### Test scenarios

- `X` wins one row.
- `O` wins one column.
- A player wins a diagonal.
- A move to an occupied cell is rejected and does not change the turn.
- A move after a win is rejected.
- Nine legal moves with no winner produce a draw.

## Entities

- Game
- Board
- Player
- GameState (IN_PROGRESS, WIN, DRAW)
- Symbol

# Class diagram

Game
- board - Board
- player1 : Player
- player2 : Player
- currentPlayer : Player
- gameState : GameState
- winner : Player
+ Game(player1, player2) -> creates a 3x3 board and sets state to IN_PROGRESS
+ makeMove(player, row, column): void
+ getGameState(): GameState
+ getWinner(): Player
+ getBoard(): Board

Board
- rows: int
- columns: int
- grid: [row][column] of Symbol
+ isValidMove(row, column) : bool - should only check cell empty and inside bounds (other 2 checks belong to game)
+ placeMove(row, column, symbol) : void
+ isBoardFull() : bool
+ checkWin (row, column, symbol) : bool

Player
- name: string
- symbol: Symbol

Symbol (enum class)
- X
- O

GameState (enum class)
- IN_PROGRESS
- WIN
- DRAW
