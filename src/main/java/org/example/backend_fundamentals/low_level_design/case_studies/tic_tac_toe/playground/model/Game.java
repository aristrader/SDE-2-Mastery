package org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.model;

import lombok.Data;
import org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.enums.GameState;
import org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.enums.Symbol;

@Data
public class Game {
  private final Board board;
  private final Player player1;
  private final Player player2;
  private Player currentPlayer;
  private GameState gameState;
  private Player winner;

  public Game(Player player1, Player player2) {

    if(player1 == null || player2 == null || player1.getSymbol()!= Symbol.X || player2.getSymbol()!=Symbol.O){
      throw new IllegalArgumentException("Invalid value set for players when creating game.");
    }

    this.player1 = player1;
    this.player2 = player2;
    this.gameState = GameState.IN_PROGRESS;
    this.currentPlayer = player1;
    this.board = new Board(3 , 3);
  }

  public void makeMove(Player player, int row, int column) {
    if(this.gameState != GameState.IN_PROGRESS){
      throw new IllegalStateException("The game is already terminated.");
    }

    if(player!=currentPlayer){
      throw new IllegalStateException("The player making the move is not the current player");
    }

    board.placeMove(row, column, player.getSymbol());

    if(board.checkWin(row, column, player.getSymbol())){
      this.winner = player;
      this.gameState = GameState.WIN;
      return;
    }

    if(board.isBoardFull()){
      this.gameState = GameState.DRAW;
      return;
    }

    if(currentPlayer == player1){
      currentPlayer = player2;
    } else {
      currentPlayer = player1;
    }
  }
}
