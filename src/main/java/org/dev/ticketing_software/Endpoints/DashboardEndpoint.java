package org.dev.ticketing_software.Endpoints;

import org.dev.ticketing_software.Data.Tickets.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardEndpoint {

    @Autowired
    TicketRepository ticketRepository;

    @GetMapping("/")
    public String getDashboard(Model model) {
        model.addAttribute("tickets", ticketRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/create")
    public String getNewTicket() {
        return "create.html";
    }
}
