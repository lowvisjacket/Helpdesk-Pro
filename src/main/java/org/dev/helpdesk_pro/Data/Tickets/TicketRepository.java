package org.dev.ticketing_software.Data.Tickets;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.dev.ticketing_software.Enum.TicketDepartment;
import org.dev.ticketing_software.Enum.TicketStatus;
import java.util.List;
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @EntityGraph(attributePaths = "notes")
    Ticket findWithNotesById(Long id);
    List<Ticket> findByDepartment(TicketDepartment department);
    List<Ticket> findByAgentUsername(String agentUsername);

    List<Ticket> findByRequestorUsername(String requestorUsername);
    @EntityGraph(attributePaths = "notes")
    List<Ticket> findWithNotesByRequestorUsername(String requestorUsername);
    @EntityGraph(attributePaths = "notes")
    @Query("select distinct t from Ticket t")
    List<Ticket> findAllWithNotes();

    List<Ticket> findByTicketStatusNotOrderByTicketDateDescTicketTimeDesc(TicketStatus ticketStatus);

    @NativeQuery("select * from tickets where ticketStatus not in ('CLOSED', 'RESOLVED')")
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
