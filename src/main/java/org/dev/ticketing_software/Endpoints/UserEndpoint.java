package org.dev.ticketing_software.Endpoints;

import org.dev.ticketing_software.Data.Tickets.TicketRepository;
import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RequestMapping("/user")
@Controller
public class UserEndpoint {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    //retrieves user by uuid
    @GetMapping("/uuid/{uuid}")
    public Optional<User> getUserByUUID(@PathVariable String uuid) {
        return userRepository.findById(uuid);
    }

    //retrieves user by username
    @GetMapping("/username/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username);
    }

    @GetMapping("/")
    public String getUserDashboard(Model model, Principal principal) {
        model.addAttribute("tickets", ticketRepository.findByRequestorUsername(principal.getName()));
        return "enduser";
    }
}
