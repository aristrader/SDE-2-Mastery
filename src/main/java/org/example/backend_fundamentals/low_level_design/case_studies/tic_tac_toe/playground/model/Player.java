package org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.backend_fundamentals.low_level_design.case_studies.tic_tac_toe.playground.enums.Symbol;

@Data
public class Player {
  private String name;
  private Symbol symbol;
}
