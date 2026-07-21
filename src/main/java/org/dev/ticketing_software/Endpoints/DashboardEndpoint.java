package org.dev.ticketing_software.Endpoints;

import org.dev.ticketing_software.Data.Tickets.TicketRepository;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardEndpoint {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/")
    public String getDashboard(@NonNull Model model) {
        model.addAttribute("tickets", ticketRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/create")
    public String getNewTicket() {
        return "create.html";
    }

    @GetMapping("/details/{id}")
    public String getTicketDetails(@PathVariable int id, Model model) {
        model.addAttribute("ticket", ticketRepository.findByid(id));
        model.addAttribute("agents", userRepository.findByNotRole("USER"));
        System.out.println(ticketRepository.findByid(id).getRequestor());
        return "details";
    }
}
