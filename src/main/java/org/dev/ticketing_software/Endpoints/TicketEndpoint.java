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

    @GetMapping("/{id}")
    public Ticket getTicket(@PathVariable int id) {
        return ticketRepository.findByid(id);
    }

    @GetMapping("/alltickets")
    public Iterable<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @GetMapping("/department/{dept}")
    public Iterable<Ticket> getTicketByAgentID(@PathVariable String dept) {
        return ticketRepository.findByDepartment(dept);
    }

    @PostMapping("/newticket")
    public Ticket addTicket(@RequestBody Ticket newTicket) {
        System.out.println(newTicket.getDescription());
        return ticketRepository.save(new Ticket(newTicket.getTitle(), newTicket.getDescription(), newTicket.getDepartment(), newTicket.getRequestor(), newTicket.getRequestor_id()));
    }
}
