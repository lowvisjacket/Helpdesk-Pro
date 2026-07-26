package org.dev.ticketing_software.Endpoints;

import org.dev.ticketing_software.Data.Tickets.Ticket;
import org.dev.ticketing_software.Data.Tickets.TicketNote;
import org.dev.ticketing_software.Data.Tickets.TicketNoteRepository;
import org.dev.ticketing_software.Data.Tickets.TicketRepository;
import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Objects;

@RequestMapping("/tickets")
@Controller
public class TicketEndpoint {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TicketNoteRepository ticketNoteRepository;

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
    public String addTicket(Ticket newTicket, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = new Ticket();
        ticket.setRequestor(user.getFirstName() + " " + user.getLastName());
        ticket.setRequestorUsername(user.getUsername());
        ticket.setTitle(newTicket.getTitle());
        ticket.setDepartment(newTicket.getDepartment());
        ticket.setTicketStatus("OPEN");
        ticket.setImportance("LOW");
        ticket.setAgent(null);
        ticket.setAgentUsername(null);
        ticket.setTicketDate(now.toLocalDate().toString());
        ticket.setTicketTime(now.toLocalTime().withNano(0).toString());
        ticket.setNotes(null);
        ticketRepository.save(ticket);
        return "redirect:/dashboard/details/" + ticket.getId() + " ";
    }

    @PostMapping("/{id}/update")
    public String updateTicket(@ModelAttribute Ticket ticket, @PathVariable int id) {
        Ticket oldTicket = ticketRepository.findByid(id);
        oldTicket.setTicketStatus(ticket.getTicketStatus());
        oldTicket.setImportance(ticket.getImportance());
        oldTicket.setAgentUsername(ticket.getAgentUsername());
        System.out.println(ticket);
        if (!Objects.equals(ticket.getAgentUsername(), null)) {
            User user = userRepository.findByUsername(ticket.getAgentUsername());
            oldTicket.setAgent(user.getFirstName() + " " + user.getLastName());
        }
        oldTicket.setDepartment(ticket.getDepartment());
        ticketRepository.save(oldTicket);
        if (Objects.equals(ticket.getTicketStatus(), "CLOSED")) {
            return "redirect:/dashboard/";
        }
        return "redirect:/dashboard/details/" + id + " ";
    }

    @PostMapping("/{id}/newcomment")
    public String updateComment(@RequestParam String note, @PathVariable int id, Principal principal) {
        LocalDateTime now = LocalDateTime.now();
        TicketNote ticketNote1 = new TicketNote();
        ticketNote1.setNote(note);
        ticketNote1.setUsername(principal.getName());
        ticketNote1.setCreatedAt(now);
        ticketNote1.setTicket(ticketRepository.findByid(id));
        ticketNoteRepository.save(ticketNote1);
        return "redirect:/dashboard/details/" + id + " ";
    }
}
