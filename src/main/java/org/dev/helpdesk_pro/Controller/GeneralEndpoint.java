package org.dev.ticketing_software.Controller;

import org.dev.ticketing_software.Logic.TicketService;
import org.dev.ticketing_software.Logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
@PreAuthorize("isAuthenticated()")
@Controller
public class GeneralEndpoint {

    @Autowired
    TicketService ticketService;

    @Autowired
    UserService userService;

    @GetMapping("/")
    public String redirectHome(Principal principal) {
        return userService.isUsernameAssociatedWithElevatedAccount(principal.getName())
                ? "redirect:/dashboard/"
                : "redirect:/home";
    }

    @GetMapping("/home")
    @PreAuthorize("hasRole('USER')")
    public String getUserHome(Model model, Principal principal) {
        model.addAttribute("tickets", ticketService.getUserTickets(principal.getName()));
        model.addAttribute("username", principal.getName());
        return "home";
    }

    @GetMapping("/create")
    public String createTicket() {
        return "create";
    }

    @GetMapping({"/success", "/success/"})
    public String successPage() {
        return "success";
    }
}
