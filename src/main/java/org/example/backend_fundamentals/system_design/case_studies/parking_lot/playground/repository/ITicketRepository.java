package org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.repository;

import org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.model.Ticket;

public interface ITicketRepository {

  void save(Ticket ticket);

  Ticket findById(String ticketId);

  void close(String ticketId);
}
