package org.dev.ticketing_software.Data.Tickets;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Ticket findByid(int id);
    List<Ticket> findByDepartment(String dept);
    List<Ticket> findByAgentUsername(String agentUsername);

    List<Ticket> findByRequestorUsername(String requestorUsername);

    @NativeQuery("select * from tickets where ticketStatus != 'CLOSED'")
    List<Ticket> findOpenTickets();

    @NativeQuery("select * from tickets where ticketStatus = 'IN_PROGRESS'")
    List<Ticket> findInProgressTickets();

    @NativeQuery("select * from tickets where ticketStatus = 'RESOLVED'")
    List<Ticket> findResolvedTickets();

    @NativeQuery("select * from tickets where importance = 'CRITICAL'")
    List<Ticket> findCriticalTickets();

    @NativeQuery("select * from tickets where ticketStatus = 'CLOSED'")
    List<Ticket> findClosedTickets();

}
