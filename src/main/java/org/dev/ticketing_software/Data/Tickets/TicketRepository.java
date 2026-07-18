package org.dev.ticketing_software.Data.Tickets;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TicketRepository extends CrudRepository<Ticket, Integer> {
    Ticket findByid(int id);
    List<Ticket> findByDepartment(String dept);
}
