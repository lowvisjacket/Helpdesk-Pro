package org.dev.helpdesk_pro.TestData;

import org.dev.helpdesk_pro.Data.Tickets.Ticket;
import org.dev.helpdesk_pro.Data.Tickets.TicketNote;
import org.dev.helpdesk_pro.Data.Tickets.TicketRepository;
import org.dev.helpdesk_pro.Data.Users.User;
import org.dev.helpdesk_pro.Data.Users.UserRepository;
import org.dev.helpdesk_pro.Enum.TicketDepartment;
import org.dev.helpdesk_pro.Enum.TicketImportance;
import org.dev.helpdesk_pro.Enum.TicketStatus;
import org.dev.helpdesk_pro.Enum.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/** Demo tickets for the "demo" profile. Only runs when there are no tickets yet. */
@Component
@Profile("demo")
@Order(3)
public class TicketSeeder implements CommandLineRunner {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketSeeder(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (ticketRepository.count() > 0) {
            return;
        }
        List<User> requesters = new ArrayList<>();
        List<User> agents = new ArrayList<>();
        userRepository.findAll().forEach(user -> (user.getRole() == UserRole.USER ? requesters : agents).add(user));
        if (requesters.isEmpty() || agents.isEmpty()) {
            return;
        }

        Object[][] rows = {
                {"VPN disconnects every few minutes on office Wi-Fi", "Since this morning the VPN drops every 3 to 5 minutes on the office Wi-Fi. A wired connection works fine. I have a client call at 11.", TicketDepartment.INFORMATION_TECHNOLOGY, TicketStatus.IN_PROGRESS, TicketImportance.CRITICAL, true},
                {"Laptop won't boot after last night's update", "The laptop shows the logo and then restarts in a loop.", TicketDepartment.INFORMATION_TECHNOLOGY, TicketStatus.OPEN, TicketImportance.HIGH, false},
                {"Request access to the finance shared drive", "I joined the finance team this week and need read access to the month-end folder.", TicketDepartment.FINANCE, TicketStatus.PENDING, TicketImportance.MEDIUM, true},
                {"Badge reader at the east entrance rejects new cards", "Three new starters could not badge in this morning.", TicketDepartment.SECURITY, TicketStatus.OPEN, TicketImportance.HIGH, true},
                {"Second monitor not detected after docking", "The dock powers the laptop but the external display stays black.", TicketDepartment.ENGINEERING, TicketStatus.IN_PROGRESS, TicketImportance.LOW, true},
                {"Printer on the third floor is offline", "Jobs stay in the queue and the panel shows a network error.", TicketDepartment.MAINTENANCE, TicketStatus.OPEN, TicketImportance.LOW, false},
                {"Password reset for the HR portal", "The reset email never arrives.", TicketDepartment.HUMAN_RESOURCES, TicketStatus.RESOLVED, TicketImportance.MEDIUM, true},
                {"Order equipment for two new starters", "Two engineers start on the 1st and need laptops, docks and headsets.", TicketDepartment.ENGINEERING, TicketStatus.PENDING, TicketImportance.MEDIUM, true},
                {"Lab results screen freezes when filtering by ward", "The results view hangs for about a minute when a ward filter is applied.", TicketDepartment.CLINICAL_INFORMATICS, TicketStatus.OPEN, TicketImportance.CRITICAL, false},
                {"Campaign shared mailbox is over its quota", "The marketing mailbox bounces incoming mail.", TicketDepartment.MARKETING, TicketStatus.PAUSED, TicketImportance.LOW, true},
                {"Conference room display shows no signal", "Room 4B display does not pick up HDMI or wireless casting.", TicketDepartment.MAINTENANCE, TicketStatus.RESOLVED, TicketImportance.LOW, true},
                {"Expense tool rejects receipts larger than 5 MB", "Uploading a scanned receipt fails with a generic error.", TicketDepartment.FINANCE, TicketStatus.CLOSED, TicketImportance.MEDIUM, true},
        };

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            User requester = requesters.get((i * 5) % requesters.size());
            User agent = agents.get(i % Math.min(agents.size(), 4));
            Ticket ticket = new Ticket((String) rows[i][0], (String) rows[i][1], (TicketDepartment) rows[i][2],
                    requester.getFirstName() + " " + requester.getLastName(), requester.getUsername());
            ticket.setTicketStatus((TicketStatus) rows[i][3]);
            ticket.setImportance((TicketImportance) rows[i][4]);
            if ((boolean) rows[i][5]) {
                ticket.setAgent(agent.getFirstName() + " " + agent.getLastName());
                ticket.setAgentUsername(agent.getUsername());
            }
            ticket.setTicketDate(today.minusDays(i / 2).toString());
            ticket.setTicketTime(String.format("%02d:%02d:00", 14 - i % 6, (i * 13) % 60));
            ticket.touch(requester.getUsername(), false);
            if (i == 0) {
                note(ticket, agent.getUsername(), false, "Thanks for the detail. Which access point are you closest to, and are you on the 2.4 or 5 GHz network?", "14:12:00");
                note(ticket, agents.get(1 % agents.size()).getUsername(), true, "AP-3F-02 had a firmware push last night. Likely related, rolling it back now.", "14:19:00");
                note(ticket, requester.getUsername(), false, "Third floor, near the kitchen. 5 GHz I think.", "14:26:00");
                note(ticket, agent.getUsername(), false, "The access point has been rolled back. Could you reconnect and tell me if it stays up?", "14:41:00");
            }
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);
    }

    private static void note(Ticket ticket, String username, boolean internal, String text, String time) {
        TicketNote note = new TicketNote(ticket, text, username);
        note.setInternalNote(internal);
        note.setTicketNoteDate(ticket.getTicketDate());
        note.setTicketNoteTime(time);
        ticket.getNotes().add(note);
    }
}
