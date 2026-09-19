package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service;

import java.math.BigDecimal;
import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Split;

public interface SplitStrategy {
   List<Split> calculate(BigDecimal amount, List<Split> splits);
}
