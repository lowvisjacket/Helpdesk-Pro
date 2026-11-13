package org.dev.helpdesk_pro;

import org.dev.helpdesk_pro.Data.Tickets.Ticket;
import org.dev.helpdesk_pro.Data.Users.User;
import org.dev.helpdesk_pro.Data.Users.UserRepository;
import org.dev.helpdesk_pro.Data.Views.SavedView;
import org.dev.helpdesk_pro.Data.Views.SavedViewRepository;
import org.dev.helpdesk_pro.Enum.TicketDepartment;
import org.dev.helpdesk_pro.Enum.UserRole;
import org.dev.helpdesk_pro.Logic.TicketService;
import org.dev.helpdesk_pro.Utility.Fmt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RedesignFeatureTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SavedViewRepository savedViewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TicketService ticketService;

    /** Accounts whose database role matches the role they sign in with. */
    @BeforeEach
    void accounts() {
        account("rq.requester", UserRole.USER);
        account("rq.tech", UserRole.TECHNICIAN);
        account("rq.admin", UserRole.ADMIN);
        account("rq.sys", UserRole.SYSADMIN);
    }

    private void account(String username, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Test");
        user.setLastName(role.name());
        user.setDepartment("FINANCE");
        user.setRole(role);
        user.setPassword("!");
        user.setAccountStatus(true);
        user.setLoginAttempts(0L);
        userRepository.save(user);
    }

    private Long newTicket() {
        Ticket ticket = new Ticket();
        ticket.setTitle("Printer offline");
        ticket.setDescription("The third floor printer is offline.");
        ticket.setDepartment(TicketDepartment.FINANCE);
        ticket.setRequestor("Test Requester");
        return ticketService.createNewTicket(ticket, "rq.requester").getId();
    }

    private static RequestPostProcessor requester() {
        return user("rq.requester").roles("USER");
    }

    private static RequestPostProcessor technician() {
        return user("rq.tech").roles("TECHNICIAN");
    }

    private static RequestPostProcessor sysadmin() {
        return user("rq.sys").roles("SYSADMIN");
    }

    @Test
    void everyPageRendersForTheRolesThatMayOpenIt() throws Exception {
        Long id = newTicket();
        for (String url : List.of("/dashboard/", "/dashboard/?view=0&page=2",
                "/dashboard/?f=1&status=OPEN&importance=LOW&col=status&q=printer&sort=priority",
                "/dashboard/users", "/dashboard/users/rq.requester", "/dashboard/reports", "/dashboard/details/" + id,
                "/dashboard/create", "/settings")) {
            mockMvc.perform(get(url).with(technician())).andExpect(status().isOk());
        }
        for (String url : List.of("/dashboard/users", "/dashboard/users/create", "/dashboard/users/rq.requester", "/settings")) {
            mockMvc.perform(get(url).with(sysadmin())).andExpect(status().isOk());
        }
        for (String url : List.of("/home", "/create", "/tickets/" + id, "/settings", "/success")) {
            mockMvc.perform(get(url).with(requester())).andExpect(status().isOk());
        }
        mockMvc.perform(get("/auth/login")).andExpect(status().isOk()).andExpect(content().string(containsString("Sign in")));
        mockMvc.perform(get("/403")).andExpect(status().isForbidden()).andExpect(content().string(containsString("have access")));
        mockMvc.perform(get("/dashboard/details/999999").with(technician())).andExpect(status().isNotFound())
                .andExpect(content().string(containsString("find that")));
    }

    @Test
    void requesterNeverSeesStaffOnlyNavigationOrPages() throws Exception {
        mockMvc.perform(get("/home").with(requester()))
                .andExpect(content().string(not(containsString("/dashboard/users"))))
                .andExpect(content().string(not(containsString("/dashboard/reports"))));
        mockMvc.perform(get("/dashboard/").with(requester())).andExpect(status().isForbidden());
        mockMvc.perform(post("/views").with(requester()).with(csrf()).param("name", "Mine")).andExpect(status().isForbidden());
    }

    @Test
    void internalNotesAreWithheldFromTheRequester() throws Exception {
        Long id = newTicket();
        String secret = "internal-only-diagnosis-7f3a";
        mockMvc.perform(post("/tickets/" + id + "/newnote").with(technician()).with(csrf()).param("note", secret).param("internal", "true"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/dashboard/details/" + id).with(technician())).andExpect(content().string(containsString(secret)));
        mockMvc.perform(get("/api/v1/tickets/" + id).with(technician())).andExpect(content().string(containsString(secret)));
        mockMvc.perform(get("/tickets/" + id).with(requester())).andExpect(status().isOk())
                .andExpect(content().string(not(containsString(secret))))
                .andExpect(content().string(not(containsString("Internal note"))));
        mockMvc.perform(get("/api/v1/tickets/" + id).with(requester())).andExpect(content().string(not(containsString(secret))));
        mockMvc.perform(get("/api/v1/tickets").with(requester())).andExpect(content().string(not(containsString(secret))));
        // The internal note must not even show up as a change for the requester.
        mockMvc.perform(get("/api/v1/tickets/changes?since=1").with(requester()))
                .andExpect(content().string(not(containsString("\"id\":" + id + ","))));

        // A requester cannot mark their own note internal.
        mockMvc.perform(post("/tickets/" + id + "/newnote").with(requester()).with(csrf()).param("note", "visible-reply-5521").param("internal", "true"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/tickets/" + id).with(requester())).andExpect(content().string(containsString("visible-reply-5521")));
    }

    @Test
    void changesEndpointReportsUpdatesMadeBySomeoneElse() throws Exception {
        Long id = newTicket();
        // Staff hear about the new ticket; the requester who created it does not.
        mockMvc.perform(get("/api/v1/tickets/changes?since=1").with(technician()))
                .andExpect(jsonPath("$.changes[?(@.id == " + id + ")].created").value(true));
        mockMvc.perform(get("/api/v1/tickets/changes?since=1").with(requester()))
                .andExpect(jsonPath("$.changes[?(@.id == " + id + ")]").isEmpty());

        mockMvc.perform(post("/tickets/" + id + "/newnote").with(technician()).with(csrf()).param("note", "public reply"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/api/v1/tickets/changes?since=1").with(requester()))
                .andExpect(jsonPath("$.changes[?(@.id == " + id + ")]").isNotEmpty());
        // The person who made the change is not notified about it.
        mockMvc.perform(get("/api/v1/tickets/changes?since=1").with(technician()))
                .andExpect(jsonPath("$.changes[?(@.id == " + id + ")]").isEmpty());
        // Without a starting point nothing is reported.
        mockMvc.perform(get("/api/v1/tickets/changes").with(requester())).andExpect(jsonPath("$.changes.length()").value(0));
        mockMvc.perform(get("/api/v1/tickets/changes")).andExpect(status().isUnauthorized());
    }

    @Test
    void savedViewsCanBeCreatedMadeDefaultRenamedReorderedAndDeleted() throws Exception {
        mockMvc.perform(post("/views").with(technician()).with(csrf()).param("name", "Critical only")
                        .param("filters", "importance=CRITICAL&status=BOGUS").param("columns", "status,priority,nope").param("sort", "priority")
                        .param("makeDefault", "true"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/views").with(technician()).with(csrf()).param("name", "Second")).andExpect(status().is3xxRedirection());

        List<SavedView> views = savedViewRepository.findByUsernameOrderByDisplayOrderAscIdAsc("rq.tech");
        assertEquals(2, views.size());
        SavedView first = views.get(0);
        assertEquals("importance=CRITICAL", first.getFilters());
        assertEquals("status,priority", first.getVisibleColumns());
        assertTrue(first.isDefaultView());

        // The list opens with the default view.
        mockMvc.perform(get("/dashboard/").with(technician())).andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1>Critical only</h1>")));

        mockMvc.perform(post("/views/" + first.getId() + "/rename").with(technician()).with(csrf()).param("name", "Renamed"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/views/" + first.getId() + "/move").with(technician()).with(csrf()).param("dir", "down"))
                .andExpect(status().is3xxRedirection());
        views = savedViewRepository.findByUsernameOrderByDisplayOrderAscIdAsc("rq.tech");
        assertEquals("Second", views.get(0).getName());
        assertEquals("Renamed", views.get(1).getName());

        // Another user cannot touch this view.
        mockMvc.perform(post("/views/" + first.getId() + "/delete").with(sysadmin()).with(csrf())).andExpect(status().isBadRequest());
        assertEquals(2, savedViewRepository.findByUsernameOrderByDisplayOrderAscIdAsc("rq.tech").size());

        for (SavedView view : views) {
            mockMvc.perform(post("/views/" + view.getId() + "/delete").with(technician()).with(csrf())).andExpect(status().is3xxRedirection());
        }
        assertTrue(savedViewRepository.findByUsernameOrderByDisplayOrderAscIdAsc("rq.tech").isEmpty());
    }

    @Test
    void themePreferenceIsStoredAndAppliedServerSide() throws Exception {
        mockMvc.perform(post("/settings/preferences").with(requester()).with(csrf()).param("theme", "dark").param("soundMuted", "true"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/home").with(requester()))
                .andExpect(content().string(containsString("<html lang=\"en\" class=\"dark\">")))
                .andExpect(content().string(containsString("data-muted=\"true\"")));
        mockMvc.perform(post("/settings/preferences").with(requester()).with(csrf()).param("theme", "system").param("soundMuted", "false"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/home").with(requester())).andExpect(content().string(containsString("<html lang=\"en\">")));
    }

    @Test
    void onlyASysadminCanCreateASysadminAccount() throws Exception {
        mockMvc.perform(post("/dashboard/users/create").with(user("rq.admin").roles("ADMIN")).with(csrf())
                        .param("firstName", "Eve").param("lastName", "Escalate").param("username", "eve.escalate")
                        .param("password", "a-long-password-1").param("department", "FINANCE").param("role", "SYSADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    void overlongTicketTextIsRejectedWithAReadableError() throws Exception {
        mockMvc.perform(post("/tickets/newticket").with(requester()).with(csrf()).param("requestor", "Test Requester")
                        .param("title", "x".repeat(201)).param("department", "FINANCE").param("description", "d"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("200 characters or fewer")));
    }

    @Test
    void initialsAndLabelsCopeWithRealNames() {
        Fmt fmt = new Fmt();
        assertEquals("AW", fmt.initials("Aleksandra Wiśniewska-Kowalczyk"));
        assertEquals("CM", fmt.initials("Christopher Alexander Montgomery III"));
        assertEquals("J", fmt.initials("Jo"));
        assertEquals("FL", fmt.initials("🦊 Fox lee"));
        assertEquals("王", fmt.initials("王秀英"));
        assertEquals("SL", fmt.initials("  Sam   Lee "));
        assertEquals("WT", fmt.initials("wc.tech"));
        assertEquals("?", fmt.initials(null));
        assertEquals("In progress", fmt.label("IN_PROGRESS"));
        assertEquals("Paramètres de sécurité", fmt.label("Paramètres de sécurité"));
        assertEquals("1,284 tickets", fmt.count(1284, "ticket", "tickets"));
        assertEquals("1 ticket", fmt.count(1, "ticket", "tickets"));
    }
}
