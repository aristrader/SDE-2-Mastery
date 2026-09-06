package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.fees;

import java.time.Instant;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.Ticket;

public interface FeeCalculator {
  Double collect(Ticket ticket, Instant exitTime);
}
