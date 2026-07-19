package org.dev.ticketing_software.Endpoints;

import org.dev.ticketing_software.Data.Tickets.Ticket;
import org.dev.ticketing_software.Data.Tickets.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/tickets")
@RestController
public class TicketEndpoint {

    @Autowired
    TicketRepository ticketRepository;

    //returns a specific ticket by ID
    @GetMapping("/{id}")
    public Ticket getTicket(@PathVariable int id) {
        return ticketRepository.findByid(id);
    }

    //returns all tickets (mainly for dashboard)
    @GetMapping("/alltickets")
    public Iterable<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    //returns tickets by its respective department
    @GetMapping("/department/{dept}")
    public Iterable<Ticket> getTicketByAgentID(@PathVariable String dept) {
        return ticketRepository.findByDepartment(dept);
    }

    //post to submit a new ticket
    @PostMapping("/newticket")
    public Ticket addTicket(@RequestBody Ticket newTicket) {
        return ticketRepository.save(new Ticket(newTicket.getTitle(), newTicket.getDescription(), newTicket.getDepartment(), newTicket.getRequestor(), newTicket.getRequestor_id()));
    }
}
