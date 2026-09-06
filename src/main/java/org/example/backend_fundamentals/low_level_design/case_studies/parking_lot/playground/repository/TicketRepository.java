package org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.repository;

import java.util.HashMap;
import java.util.Map;
import org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.model.Ticket;
import org.springframework.stereotype.Repository;

@Repository
public class TicketRepository implements ITicketRepository {

  private final Map<String, Ticket> ticketMap = new HashMap<>();

  @Override
  public void save(Ticket ticket) {
    if (ticket == null) {
      throw new IllegalArgumentException("ticket is required");
    }

    ticketMap.put(ticket.getId(), ticket);
  }

  @Override
  public Ticket findById(String ticketId) {
    if(ticketMap.containsKey(ticketId)) {
      return ticketMap.get(ticketId);
    }
    throw new RuntimeException("Invalid ticket");
  }

  @Override
  public void close(String ticketId) {
    ticketMap.remove(ticketId);
  }
}
