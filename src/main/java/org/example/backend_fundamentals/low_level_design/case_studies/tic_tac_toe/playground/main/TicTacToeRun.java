package org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.main;

import org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.enums.Symbol;
import org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.model.Game;
import org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.model.Player;

public class TicTacToeRun {

  public static void main(String[] args) {
    demonstrateRowWinAndRejections();
    demonstrateColumnWin();
    demonstrateDiagonalWin();
    demonstrateDraw();
  }

  private static void demonstrateRowWinAndRejections() {
    Player x = player("Alice", Symbol.X);
    Player o = player("Bob", Symbol.O);
    Game game = new Game(x, o);

    expectFailure(() -> game.makeMove(o, 0, 0), "wrong player turn");
    expectFailure(() -> game.makeMove(x, -1, 0), "out-of-bounds move");

    game.makeMove(x, 0, 0);
    expectFailure(() -> game.makeMove(o, 0, 0), "occupied cell");

    game.makeMove(o, 1, 0);
    game.makeMove(x, 0, 1);
    game.makeMove(o, 1, 1);
    game.makeMove(x, 0, 2);

    printOutcome("Row win", game);

    expectFailure(() -> game.makeMove(x, 2, 2), "move after game completion");
  }

  private static void demonstrateColumnWin() {
    Player x = player("Carol", Symbol.X);
    Player o = player("Dan", Symbol.O);
    Game game = new Game(x, o);

    game.makeMove(x, 0, 0);
    game.makeMove(o, 0, 1);
    game.makeMove(x, 1, 0);
    game.makeMove(o, 1, 1);
    game.makeMove(x, 2, 2);
    game.makeMove(o, 2, 1);

    printOutcome("Column win", game);
  }

  private static void demonstrateDiagonalWin() {
    Player diagonalX = player("Eve", Symbol.X);
    Player diagonalO = player("Frank", Symbol.O);
    Game diagonalGame = new Game(diagonalX, diagonalO);

    diagonalGame.makeMove(diagonalX, 0, 0);
    diagonalGame.makeMove(diagonalO, 0, 1);
    diagonalGame.makeMove(diagonalX, 1, 1);
    diagonalGame.makeMove(diagonalO, 0, 2);
    diagonalGame.makeMove(diagonalX, 2, 2);

    printOutcome("Diagonal win", diagonalGame);
  }

  private static void demonstrateDraw() {
    Player x = player("Grace", Symbol.X);
    Player o = player("Henry", Symbol.O);
    Game game = new Game(x, o);

    game.makeMove(x, 0, 0);
    game.makeMove(o, 0, 1);
    game.makeMove(x, 0, 2);
    game.makeMove(o, 1, 1);
    game.makeMove(x, 1, 0);
    game.makeMove(o, 1, 2);
    game.makeMove(x, 2, 1);
    game.makeMove(o, 2, 0);
    game.makeMove(x, 2, 2);

    printOutcome("Draw", game);
  }

  private static void printOutcome(String scenario, Game game) {
    System.out.println(scenario + " board: " + game.getBoard().getGrid());
    System.out.println(scenario + " state: " + game.getGameState());
    System.out.println(scenario + " winner: " + game.getWinner());
  }

  private static Player player(String name, Symbol symbol) {
    Player player = new Player();
    player.setName(name);
    player.setSymbol(symbol);
    return player;
  }

  private static void expectFailure(Runnable action, String scenario) {
    try {
      action.run();
    } catch (IllegalArgumentException | IllegalStateException expected) {
      System.out.println("Rejected " + scenario + ": " + expected.getMessage());
      return;
    }

    throw new IllegalStateException("Expected failure for " + scenario);
  }
}
