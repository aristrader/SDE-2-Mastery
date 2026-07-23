package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.Ticket;

public class TicketRepository implements ITicketRepository {

  private final Map<String, Ticket> ticketMap = new HashMap<>();

  @Override
  public void save(Ticket ticket) {
    if (ticket == null || ticket.getId() == null) {
      throw new IllegalArgumentException("ticket and ticket id are required");
    }

    ticketMap.put(ticket.getId(), ticket);
  }

  @Override
  public Optional<Ticket> findById(String ticketId) {
    return Optional.ofNullable(ticketMap.get(ticketId));
  }
}
