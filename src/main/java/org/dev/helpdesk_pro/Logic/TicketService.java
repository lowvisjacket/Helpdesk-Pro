package org.dev.ticketing_software.Logic;

import org.dev.ticketing_software.Data.Tickets.Ticket;
import org.dev.ticketing_software.Data.Tickets.TicketNote;
import org.dev.ticketing_software.Data.Tickets.TicketNoteRepository;
import org.dev.ticketing_software.Data.Tickets.TicketRepository;
import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.dev.ticketing_software.Enum.TicketDepartment;
import org.dev.ticketing_software.Enum.TicketImportance;
import org.dev.ticketing_software.Enum.TicketStatus;
import org.dev.ticketing_software.Enum.UserRole;
import org.dev.ticketing_software.Exceptions.TicketException;
import org.dev.ticketing_software.Exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TicketNoteRepository ticketNoteRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, UserService userService, TicketNoteRepository ticketNoteRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.ticketNoteRepository = ticketNoteRepository;
    }

    public Ticket getTicketForUser(Long id, String username) throws AccessDeniedException {
         return getTicket(id, username);
    }

    public Ticket createNewTicket(Ticket newTicket, String username) {
        validateRequestorName(newTicket.getRequestor());
        validateRequestorUsername(username);
        validateTitle(newTicket.getTitle());
        validateDescription(newTicket.getDescription());
        validateDepartment(newTicket.getDepartment());

        return ticketRepository.save(new Ticket(
                newTicket.getTitle(),
                newTicket.getDescription(),
                newTicket.getDepartment(),
                newTicket.getRequestor(),
                username
            ));
    }

    public void updateTicket(Ticket ticket, Long Id, String username) throws AccessDeniedException {
        Ticket oldTicket = getTicket(Id, username);

        //run validations
        validateTicketStatus(ticket.getTicketStatus());
        validateTicketImportance(ticket.getImportance());
        validateDepartment(ticket.getDepartment());
        if (ticket.getAgentUsername() != null && !ticket.getAgentUsername().isBlank()) {
            userService.validateAgentUsername(ticket.getAgentUsername());
        }

        oldTicket.setTicketStatus(ticket.getTicketStatus());
        oldTicket.setImportance(ticket.getImportance());
        oldTicket.setDepartment(ticket.getDepartment());
        if (ticket.getAgentUsername() != null && ticket.getAgentUsername().isBlank()) {
            oldTicket.setAgentUsername(null);
            oldTicket.setAgent(null);
        } else if (ticket.getAgentUsername() != null) {
            String agentName = userService.getAgentName(ticket.getAgentUsername());
            oldTicket.setAgentUsername(ticket.getAgentUsername());
            oldTicket.setAgent(agentName);
        }

        ticketRepository.save(oldTicket);
    }

    public void newNote(String note, String username, Long id) throws AccessDeniedException {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Note must not be blank.");
        }
        Ticket ticket = getTicket(id, username);
        validateIfUserCanAccessTicket(username, ticket);
        TicketNote ticketNote = new TicketNote(ticket, note, username);
        ticketNoteRepository.save(ticketNote);
    }

    private void validateTicketStatus(TicketStatus ticketStatus) {
        if (ticketStatus == null) {
            throw new IllegalArgumentException("Ticket status must not be null");
        }
    }

    private void validateTicketImportance(TicketImportance ticketImportance) {
        if (ticketImportance == null) {
            throw new IllegalArgumentException("Ticket importance must not be null");
        }
    }

    private void validateRequestorName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Requestor name must not be blank.");
        }
    }

    private void validateRequestorUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User does not exist.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or empty.");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null or empty.");
        }
    }

    private void validateDepartment(TicketDepartment ticketDepartment) {
        if (ticketDepartment == null) {
            throw new IllegalArgumentException("Department must be valid.");
        }
    }

    private Ticket getTicket(Long id, String username) throws TicketException, AccessDeniedException {
        Ticket ticket = ticketRepository.findWithNotesById(id);
        if (ticket == null) {
            throw new TicketException("Ticket not found");
        }
        validateIfUserCanAccessTicket(username, ticket);
        return ticket;
    }

    public TicketStatus getTicketStatus(Long id, String username) throws AccessDeniedException {
        Ticket ticket = getTicket(id, username);
        return ticket.getTicketStatus();
    }

    private void validateIfUserCanAccessTicket(String username, Ticket ticket) throws AccessDeniedException {
        UserRole userRole = userService.getUserRole(username);
        if (!userRole.equals(UserRole.USER)) {
            return;
        }
        if (!ticket.getRequestorUsername().equals(username)) {
            throw new AccessDeniedException("User does not have sufficient permissions to edit or view this ticket");
        }
    }

    public List<Ticket> getUserTickets(String username) {
        userService.validateIfUserExists(username);
        return ticketRepository.findWithNotesByRequestorUsername(username);
    }
}
