package org.dev.ticketing_software.Endpoints;

import org.dev.ticketing_software.Data.Tickets.TicketRepository;
import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard")
public class DashboardEndpoint {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @Cacheable
    @GetMapping("/")
    public String getDashboard(@NonNull Model model) {
        model.addAttribute("tickets", ticketRepository.findOpenTickets());
        model.addAttribute("totalTickets", ticketRepository.findOpenTickets().size());
        return "dashboard";
    }

    @GetMapping("/create")
    public String getNewTicket() {
        return "create.html";
    }

    @GetMapping("/users")
    public String getUsers(@NonNull Model model, Principal principal) {
        System.out.println(principal);
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN','SYSADMIN')")
    @GetMapping("/users/{username}")
    public String getUserDetails(@NonNull Model model, @PathVariable String username) {
        model.addAttribute("user", userRepository.findByUsername(username));
        return "userDetails";
    }

    @PreAuthorize("hasAnyRole('ADMIN','SYSADMIN')")
    @PostMapping("/users/{username}/update")
    public String updateUser(@ModelAttribute User user, @PathVariable String username) {
        User oldUser = userRepository.findByUsername(username);
        oldUser.setDepartment(user.getDepartment());
        oldUser.setFirstName(user.getFirstName());
        oldUser.setLastName(user.getLastName());
        oldUser.setRole(user.getRole());
        userRepository.save(oldUser);
        return "redirect:/dashboard/users/" + username + " ";
    }

    @Cacheable
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SYSADMIN')")
    @PostMapping("/users/{username}/toggle")
    public String toggleUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        user.setAccountStatus(!user.isAccountStatus());
        userRepository.save(user);
        return "redirect:/dashboard/users/" + username + " ";
    }

    @GetMapping("/details/{id}")
    public String getTicketDetails(@PathVariable int id, Model model) {
        model.addAttribute("ticket", ticketRepository.findByid(id));
        model.addAttribute("agents", userRepository.findByNotRole("USER"));
        System.out.println(ticketRepository.findByid(id).getRequestor());
        return "details";
    }

    @Cacheable
    @GetMapping("/reports")
    public String getReports(@NonNull Model model) {
        long totalTickets = ticketRepository.count();

        long openTickets = ticketRepository.findOpenTickets().size();

        long progressTickets = ticketRepository.findInProgressTickets().size();

        long resolvedTickets = ticketRepository.findResolvedTickets().size();

        long criticalTickets = ticketRepository.findCriticalTickets().size();

        long closedTickets = ticketRepository.findClosedTickets().size();

        double closedPercentage = 0;

        if (totalTickets > 0) {
            closedPercentage = (closedTickets * 100.0) / totalTickets;
        }

        model.addAttribute("closedTickets", closedTickets);
        model.addAttribute("closedPercentage", String.format("%.1f", closedPercentage));


        double openPercent = totalTickets == 0 ? 0 :
                (openTickets * 100.0) / totalTickets;


        double progressPercent = totalTickets == 0 ? 0 :
                (progressTickets * 100.0) / totalTickets;


        double resolvedPercent = totalTickets == 0 ? 0 :
                (resolvedTickets * 100.0) / totalTickets;


        double criticalPercent = totalTickets == 0 ? 0 :
                (criticalTickets * 100.0) / totalTickets;

        model.addAttribute("totalTickets", ticketRepository.findAll().size());
        model.addAttribute("openTickets", ticketRepository.findOpenTickets().size());
        model.addAttribute("progressTickets", ticketRepository.findInProgressTickets().size());
        model.addAttribute("resolvedTickets", ticketRepository.findResolvedTickets().size());
        model.addAttribute("criticalTickets", ticketRepository.findCriticalTickets().size());
        model.addAttribute("topUsers", userRepository.findTopTicketSubmitters());
        model.addAttribute("enabledUsers", userRepository.findEnabledAccountsCount().size());
        model.addAttribute("disabledUsers", userRepository.findDisabledAccountsCount().size());
        model.addAttribute("openPercent", openPercent);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("resolvedPercent", resolvedPercent);
        model.addAttribute("criticalPercent", criticalPercent);
        return "reports";
    }
}
