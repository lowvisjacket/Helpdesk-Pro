package org.dev.ticketing_software.Controller;

import org.dev.ticketing_software.Data.Tickets.TicketRepository;
import org.dev.ticketing_software.Data.Tickets.Ticket;
import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardEndpoint {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DashboardEndpoint(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String getDashboard(@NonNull Model model) {
        List<Ticket> tickets = ticketRepository.findOpenTickets();
        model.addAttribute("tickets", tickets);
        model.addAttribute("totalTickets", tickets.size());
        return "dashboard";
    }

    @GetMapping("/create")
    public String getNewTicket() {
        return "create.html";
    }

    @GetMapping("/users")
    public String getUsers(@NonNull Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @PreAuthorize("hasAnyRole('ADMIN','SYSADMIN')")
    @GetMapping("/users/create")
    public String getCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "createUser";
    }

    @PreAuthorize("hasAnyRole('ADMIN','SYSADMIN')")
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user) {
        if (user.getFirstName() == null || user.getFirstName().isBlank()
                || user.getLastName() == null || user.getLastName().isBlank()
                || user.getUsername() == null
                || !user.getUsername().matches("[a-zA-Z0-9._-]{3,32}")
                || user.getPassword() == null || user.getPassword().length() < 12
                || user.getDepartment() == null || user.getDepartment().isBlank()
                || user.getRole() == null) {
            throw new IllegalArgumentException("Provide a name, valid username, password of at least 12 characters, department, and role.");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username is already in use.");
        }

        User newUser = new User();
        newUser.setFirstName(user.getFirstName().trim());
        newUser.setLastName(user.getLastName().trim());
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setDepartment(user.getDepartment());
        newUser.setRole(user.getRole());
        newUser.setAccountStatus(true);
        newUser.setLoginAttempts(0L);
        userRepository.save(newUser);
        return "redirect:/dashboard/users/" + newUser.getUsername();
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
        return "redirect:/dashboard/users/" + username;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    @PostMapping("/users/{username}/toggle")
    public String toggleUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        user.setAccountStatus(!user.isAccountStatus());
        userRepository.save(user);
        return "redirect:/dashboard/users/" + username;
    }

    @PreAuthorize("hasRole('SYSADMIN')")
    @PostMapping("/users/{username}/delete")
    public String deleteUser(@PathVariable String username, Principal principal) {
        if (principal.getName().equals(username)) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new org.dev.ticketing_software.Exceptions.UserNotFoundException("User does not exist.");
        }
        userRepository.delete(user);
        return "redirect:/dashboard/users";
    }

    @GetMapping("/details/{id}")
    public String getTicketDetails(@PathVariable Long id, Model model, Principal principal) {
        model.addAttribute("ticket", ticketRepository.findWithNotesById(id));
        model.addAttribute("agents", userRepository.findByNotRole("USER"));
        model.addAttribute("username", principal.getName());
        return "details";
    }

    @GetMapping("/reports")
    public String getReports(@NonNull Model model) {
        long totalTickets = ticketRepository.count();
        long openTickets = ticketRepository.findOpenTickets().size();
        long progressTickets = ticketRepository.findInProgressTickets().size();
        long resolvedTickets = ticketRepository.findResolvedTickets().size();
        long criticalTickets = ticketRepository.findCriticalTickets().size();
        long closedTickets = ticketRepository.findClosedTickets().size();
        long enabledUsers = userRepository.countByAccountStatusTrue();
        long disabledUsers = userRepository.countByAccountStatusFalse();
        double closedPercentage = totalTickets == 0 ? 0 : (closedTickets * 100.0) / totalTickets;

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

        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("progressTickets", progressTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("criticalTickets", criticalTickets);
        model.addAttribute("topUsers", userRepository.findTopTicketSubmitters());
        model.addAttribute("enabledUsers", enabledUsers);
        model.addAttribute("disabledUsers", disabledUsers);
        model.addAttribute("openPercent", openPercent);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("resolvedPercent", resolvedPercent);
        model.addAttribute("criticalPercent", criticalPercent);
        return "reports";
    }
}
