package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.service;

import lombok.extern.slf4j.Slf4j;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.enums.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentService {
  public void collect(String ticketId, Double amount, PaymentMethod paymentMethod) {
    log.info("SUCCESSFULLY COLLECTED PAYMENT for ticketId: {} for amount: {} by PaymentMethod: {}",
        ticketId, amount, paymentMethod);
  }
}
