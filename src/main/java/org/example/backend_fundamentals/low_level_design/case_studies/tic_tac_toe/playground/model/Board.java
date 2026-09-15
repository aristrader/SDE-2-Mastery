package org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import lombok.Data;
import org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.enums.Symbol;

@Data
public class Board {
  private int rows;
  private int columns;
  private ArrayList<ArrayList<Symbol>> grid;

  Board(int rows, int columns){
    this.rows = rows;
    this.columns = columns;
    ArrayList<ArrayList<Symbol>> newGrid = new ArrayList<>();
    for(int i=0;i<rows;i++){
      ArrayList<Symbol> temp = new ArrayList<>();
      for(int j=0; j<columns;j++){
        temp.add(null);
      }
      newGrid.add(temp);
    }
    this.grid = newGrid;
  }

  public boolean isValidMove(int row, int column) {
    if(row<0 || row>=this.rows || column<0 || column>=this.columns){
      return false;
    }
    return this.grid.get(row).get(column) == null;
  }

  public void placeMove(int row, int column, Symbol symbol) {
    if(!this.isValidMove(row, column)){
      throw new IllegalArgumentException("Invalid value for row and column");
    }

    this.grid.get(row).set(column, symbol);
  }

  // Can be simplfied to keep count addition in place move function and once the count is equal to rows*columns, we can say that the board is full instead of traversing everytime.
  public boolean isBoardFull() {
    for(int i=0;i<rows;i++){
      for(int j=0;j<columns;j++){
        if(grid.get(i).get(j) == null){
          return false;
        }
      }
    }
    return true;
  }

  public boolean checkWin(int row, int column, Symbol symbol){
    long countCol = grid.get(row).stream()
        .filter(sym -> Objects.equals(sym, symbol))
        .count();

    int rowCount = 0;
    for (int i = 0; i < rows; i++) {
      if(grid.get(i).get(column) == symbol){
        rowCount++;
      }
    }

    boolean mainDiagonalWin = false;
    boolean antiDiagonalWin = false;

    if (rows == columns && row == column) {
      mainDiagonalWin = true;
      for (int i = 0; i < rows; i++) {
        if (grid.get(i).get(i) != symbol) {
          mainDiagonalWin = false;
          break;
        }
      }
    }

    if (rows == columns && row + column == columns - 1) {
      antiDiagonalWin = true;
      for (int i = 0; i < rows; i++) {
        if (grid.get(i).get(columns - 1 - i) != symbol) {
          antiDiagonalWin = false;
          break;
        }
      }
    }

    return rowCount == rows || countCol == columns || mainDiagonalWin || antiDiagonalWin;
  }
}
