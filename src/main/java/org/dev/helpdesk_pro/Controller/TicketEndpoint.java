package org.dev.ticketing_software.Controller;

import org.dev.ticketing_software.Data.Tickets.Ticket;
import org.dev.ticketing_software.Enum.TicketStatus;
import org.dev.ticketing_software.Exceptions.TicketException;
import org.dev.ticketing_software.Exceptions.UserNotFoundException;
import org.dev.ticketing_software.Logic.TicketService;
import org.dev.ticketing_software.Logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@PreAuthorize("isAuthenticated()")
@RequestMapping("/tickets")
@Controller
public class TicketEndpoint {

    @Autowired
    TicketService ticketService;

    @Autowired
    UserService userService;

    @GetMapping("/{id}")
    public String getTicket(@PathVariable Long id, Principal principal, Model model) throws AccessDeniedException {
        model.addAttribute("ticket", ticketService.getTicketForUser(id, principal.getName()));
        return "details";
    }

    @PostMapping("/newticket")
    public String addTicket(Ticket newTicket, Principal principal) throws TicketException, UserNotFoundException {
        Ticket createdTicket = ticketService.createNewTicket(newTicket, principal.getName());
        if (userService.isUsernameAssociatedWithElevatedAccount(principal.getName())) {
            return "redirect:/dashboard/details/" + createdTicket.getId();
        }
        return "redirect:/tickets/" + createdTicket.getId();
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN','SYSADMIN')")
    @PostMapping("/{id}/update")
    public String updateTicket(@ModelAttribute Ticket ticket, @PathVariable Long id, Principal principal) throws AccessDeniedException {
        ticketService.updateTicket(ticket, id, principal.getName());
        if (ticketService.getTicketStatus(id, principal.getName()) == TicketStatus.CLOSED) {
            return "redirect:/dashboard/";
        }
        return "redirect:/dashboard/details/" + id;
    }

    @PostMapping("/{id}/newnote")
    public String newNote(@RequestParam String note, @PathVariable Long id, Principal principal) throws AccessDeniedException {
        ticketService.newNote(note, principal.getName(), id);

        if (userService.isUsernameAssociatedWithElevatedAccount(principal.getName())) {
            return "redirect:/dashboard/details/" + id;
        }
        return "redirect:/tickets/" + id;
    }
}
