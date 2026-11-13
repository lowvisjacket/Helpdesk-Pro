package org.dev.helpdesk_pro.Controller;

import org.dev.helpdesk_pro.Data.Tickets.Ticket;
import org.dev.helpdesk_pro.Enum.TicketStatus;
import org.dev.helpdesk_pro.Exceptions.TicketException;
import org.dev.helpdesk_pro.Exceptions.UserNotFoundException;
import org.dev.helpdesk_pro.Logic.TicketService;
import org.dev.helpdesk_pro.Logic.UserService;
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
    @Autowired
    org.dev.helpdesk_pro.Data.Users.UserRepository userRepository;

    @GetMapping("/{id}")
    public String getTicket(@PathVariable Long id, Principal principal, Model model) throws AccessDeniedException {
        model.addAttribute("ticket", ticketService.getTicketForUser(id, principal.getName()));
        if (userService.isUsernameAssociatedWithElevatedAccount(principal.getName())) {
            model.addAttribute("agents", userRepository.findByNotRole("USER"));
        }
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
    public String newNote(@RequestParam String note, @RequestParam(defaultValue = "false") boolean internal, @PathVariable Long id, Principal principal) throws AccessDeniedException {
        ticketService.newNote(note, principal.getName(), id, internal);

        if (userService.isUsernameAssociatedWithElevatedAccount(principal.getName())) {
            return "redirect:/dashboard/details/" + id;
        }
        return "redirect:/tickets/" + id;
    }
}
