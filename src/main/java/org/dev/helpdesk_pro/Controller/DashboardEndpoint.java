package org.dev.helpdesk_pro.Controller;

import org.dev.helpdesk_pro.Data.Tickets.TicketRepository;
import org.dev.helpdesk_pro.Data.Tickets.Ticket;
import org.dev.helpdesk_pro.Data.Users.User;
import org.dev.helpdesk_pro.Data.Users.UserRepository;
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
    @org.springframework.beans.factory.annotation.Autowired
    private org.dev.helpdesk_pro.Data.Views.SavedViewRepository savedViewRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private org.dev.helpdesk_pro.Logic.TicketService ticketService;

    private static final int PAGE_SIZE = 50;

    /** Only a system administrator may create, change or disable system administrator accounts. */
    private static void requireSysadminFor(org.dev.helpdesk_pro.Enum.UserRole... roles) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean sysadmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SYSADMIN".equals(authority.getAuthority()));
        if (sysadmin) {
            return;
        }
        for (org.dev.helpdesk_pro.Enum.UserRole role : roles) {
            if (role == org.dev.helpdesk_pro.Enum.UserRole.SYSADMIN) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Only a system administrator can manage system administrator accounts.");
            }
        }
    }

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
    public String getDashboard(
            @NonNull Model model,
            Principal principal,
            @RequestParam(required = false) Long view,
            @RequestParam(required = false) String f,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> importance,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) List<String> col,
            @RequestParam(defaultValue = "1") int page
    ) {
        String username = principal.getName();
        List<org.dev.helpdesk_pro.Data.Views.SavedView> views =
                savedViewRepository.findByUsernameOrderByDisplayOrderAscIdAsc(username);
        org.dev.helpdesk_pro.Data.Views.SavedView activeView = null;
        org.dev.helpdesk_pro.Data.Views.SavedView fromView = null;
        org.dev.helpdesk_pro.Logic.TicketFilter filter = org.dev.helpdesk_pro.Logic.TicketFilter.open();
        java.util.Set<String> cols = org.dev.helpdesk_pro.Logic.TicketFilter.columns(null);
        boolean custom = f != null;
        if (custom) {
            // The filter form was submitted: an ad-hoc, not yet saved, filter.
            filter = org.dev.helpdesk_pro.Logic.TicketFilter.of(status, importance, department, assignee, q, sort);
            cols = org.dev.helpdesk_pro.Logic.TicketFilter.columns(col == null ? "" : String.join(",", col));
            fromView = views.stream().filter(v -> v.getId().equals(from)).findFirst().orElse(null);
        } else {
            if (view == null) {
                activeView = views.stream().filter(org.dev.helpdesk_pro.Data.Views.SavedView::isDefaultView).findFirst().orElse(null);
            } else if (view != 0) {
                activeView = views.stream().filter(v -> v.getId().equals(view)).findFirst().orElse(null);
            }
            if (activeView != null) {
                filter = org.dev.helpdesk_pro.Logic.TicketFilter.fromQuery(activeView.getFilters(), activeView.getSortOrder());
                cols = org.dev.helpdesk_pro.Logic.TicketFilter.columns(activeView.getVisibleColumns());
            }
        }
        List<Ticket> all = ticketService.listTickets(filter, username);
        int pageCount = Math.max(1, (all.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(Math.max(page, 1), pageCount);
        int start = (current - 1) * PAGE_SIZE;
        List<Ticket> tickets = all.subList(start, Math.min(start + PAGE_SIZE, all.size()));
        // Every value below is validated (enum names, whitelisted columns, numeric ids, encoded text).
        String baseQuery;
        if (custom) {
            StringBuilder query = new StringBuilder("f=1");
            if (!filter.toQuery().isEmpty()) {
                query.append('&').append(filter.toQuery());
            }
            query.append("&sort=").append(filter.sort());
            cols.forEach(column -> query.append("&col=").append(column));
            if (fromView != null) {
                query.append("&from=").append(fromView.getId());
            }
            baseQuery = query.toString();
        } else {
            baseQuery = "view=" + (activeView == null ? 0 : activeView.getId());
        }
        model.addAttribute("tickets", tickets);
        model.addAttribute("totalTickets", all.size());
        model.addAttribute("page", current);
        model.addAttribute("pageCount", pageCount);
        model.addAttribute("firstRow", all.isEmpty() ? 0 : start + 1);
        model.addAttribute("lastRow", start + tickets.size());
        model.addAttribute("baseQuery", baseQuery);
        model.addAttribute("filter", filter);
        model.addAttribute("cols", cols);
        model.addAttribute("activeView", activeView);
        model.addAttribute("fromView", fromView);
        model.addAttribute("custom", custom);
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
        requireSysadminFor(user.getRole());
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
        User found = userRepository.findByUsername(username);
        if (found == null) {
            throw new org.dev.helpdesk_pro.Exceptions.UserNotFoundException("User does not exist.");
        }
        model.addAttribute("user", found);
        return "userDetails";
    }

    @PreAuthorize("hasAnyRole('ADMIN','SYSADMIN')")
    @PostMapping("/users/{username}/update")
    public String updateUser(@ModelAttribute User user, @PathVariable String username) {
        User oldUser = userRepository.findByUsername(username);
        if (oldUser == null) {
            throw new org.dev.helpdesk_pro.Exceptions.UserNotFoundException("User does not exist.");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("A role is required.");
        }
        requireSysadminFor(oldUser.getRole(), user.getRole());
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
        if (user == null) {
            throw new org.dev.helpdesk_pro.Exceptions.UserNotFoundException("User does not exist.");
        }
        requireSysadminFor(user.getRole());
        if (username.equals(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new IllegalArgumentException("You cannot disable your own account.");
        }
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
            throw new org.dev.helpdesk_pro.Exceptions.UserNotFoundException("User does not exist.");
        }
        savedViewRepository.deleteAll(savedViewRepository.findByUsernameOrderByDisplayOrderAscIdAsc(username));
        userRepository.delete(user);
        return "redirect:/dashboard/users";
    }

    @GetMapping("/details/{id}")
    public String getTicketDetails(@PathVariable Long id, Model model, Principal principal) throws java.nio.file.AccessDeniedException {
        model.addAttribute("ticket", ticketService.getTicketForUser(id, principal.getName()));
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
