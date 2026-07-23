package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.repository;

import java.util.Optional;
import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.Ticket;

public interface ITicketRepository {

  void save(Ticket ticket);

  Optional<Ticket> findById(String ticketId);
}
