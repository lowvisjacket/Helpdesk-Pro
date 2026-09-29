package org.dev.ticketing_software;

import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.config.location=classpath:/",
        "demo.seed.count=61"
})
@AutoConfigureMockMvc
class TicketingSoftwareApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void databaseLoginUsesStoredPasswordAndRole() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .param("username", "jsmith001")
                                .param("password", "test123")
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/"));
    }

    @Test
    void customerHomeRendersTheirTicketList() throws Exception {
        mockMvc.perform(get("/home").with(user("jsmith001").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
     void userCannotSeeOtherUsersTickets() throws Exception {
        mockMvc.perform(
                get("/tickets/1")
                        .with(user("jbrown004")
                        )
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanAccessOwnTicket() throws Exception {
        mockMvc.perform(
                get("/tickets/1")
                        .with(user("jsmith001")
                        )
        )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void technicianShouldSeeAnyTickets() throws Exception {
        mockMvc.perform(
                get("/tickets/1")
                        .with(user("psmith061"))
        )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void adminShouldSeeAnyTickets() throws Exception {
        mockMvc.perform(
                        get("/tickets/1")
                                .with(user("mhernandez031"))
                )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void sysadminShouldSeeAnyTickets() throws Exception {
        mockMvc.perform(
                        get("/tickets/1")
                                .with(user("jjackson019"))
                )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void technicianShouldBeAbleToEditTicket() throws Exception {
        mockMvc.perform(
                        post("/tickets/1/update")
                                .with(user("psmith061").roles("TECHNICIAN"))
                                .with(csrf())
                                .param("ticketStatus", "PENDING")
                                .param("importance", "LOW")
                                .param("department", "INFORMATION_TECHNOLOGY")
                                .param("agentUsername", "jsmith001")
                )
                .andExpect(redirectedUrl("/dashboard/details/1"));

    }

    @Test
    void authenticatedElevatedUserCanReadTicketApi() throws Exception {
        mockMvc.perform(
                        get("/api/v1/tickets/1")
                                .with(user("jsmith001").roles("SYSADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void unauthenticatedApiRequestGetsJsonUnauthorizedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void authenticatedUserCanCreateTicketThroughApi() throws Exception {
        mockMvc.perform(
                        post("/api/v1/tickets")
                                .with(user("jsmith001").roles("SYSADMIN"))
                                .with(csrf())
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "API-created ticket",
                                          "description": "Created by an integration test.",
                                          "department": "INFORMATION_TECHNOLOGY",
                                          "requestor": "James Smith"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.requestorUsername").value("jsmith001"));
    }

    @Test
    void apiRejectsBlankTicketFields() throws Exception {
        mockMvc.perform(
                        post("/api/v1/tickets")
                                .with(user("jsmith001").roles("SYSADMIN"))
                                .with(csrf())
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": " ",
                                          "description": "Description",
                                          "department": "INFORMATION_TECHNOLOGY",
                                          "requestor": "James Smith"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void administratorCanCreateUserWithAnEncodedPassword() throws Exception {
        mockMvc.perform(
                        post("/dashboard/users/create")
                                .with(user("jjackson019").roles("SYSADMIN"))
                                .with(csrf())
                                .param("firstName", "Taylor")
                                .param("lastName", "Jordan")
                                .param("username", "taylorj900")
                                .param("password", "a-long-demo-password")
                                .param("department", "INFORMATION_TECHNOLOGY")
                                .param("role", "USER")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/users/taylorj900"));
        User createdUser = userRepository.findByUsername("taylorj900");
        assertTrue(passwordEncoder.matches("a-long-demo-password", createdUser.getPassword()));
    }
}
