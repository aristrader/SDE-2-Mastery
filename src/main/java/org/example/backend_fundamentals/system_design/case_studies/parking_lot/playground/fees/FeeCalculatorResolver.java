package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.fees;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeeCalculatorResolver {
  private final HourlyFeeCalculator hourlyFeeCalculator;

  public FeeCalculator resolve(){
    return hourlyFeeCalculator;
  }
}
