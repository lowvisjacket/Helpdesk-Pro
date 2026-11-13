package org.dev.helpdesk_pro.Controller.Api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.dev.helpdesk_pro.Data.Tickets.Ticket;
import org.dev.helpdesk_pro.Data.Tickets.TicketRepository;
import org.dev.helpdesk_pro.Enum.TicketDepartment;
import org.dev.helpdesk_pro.Enum.TicketImportance;
import org.dev.helpdesk_pro.Enum.TicketStatus;
import org.dev.helpdesk_pro.Logic.TicketService;
import org.dev.helpdesk_pro.Logic.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@PreAuthorize("isAuthenticated()")
public class TicketApiController {
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final UserService userService;

    public TicketApiController(
            TicketRepository ticketRepository,
            TicketService ticketService,
            UserService userService
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping
    public List<TicketResponse> listTickets(Principal principal) {
        List<Ticket> tickets = userService.isUsernameAssociatedWithElevatedAccount(principal.getName())
                ? ticketRepository.findAllWithNotes()
                : ticketService.getUserTickets(principal.getName());
        return tickets.stream().map(ticket -> respond(ticket, principal)).toList();
    }

    /** Lightweight polling endpoint: tickets this user may see that someone else changed since the given time. */
    @GetMapping("/changes")
    public java.util.Map<String, Object> changes(@RequestParam(defaultValue = "0") long since, Principal principal) {
        long now = System.currentTimeMillis();
        long from = since <= 0 || since > now ? now : since;
        List<java.util.Map<String, Object>> changes = ticketService.changesSince(from, principal.getName()).stream()
                .limit(20)
                .map(ticket -> java.util.Map.<String, Object>of(
                        "id", ticket.getId(),
                        "title", ticket.getTitle(),
                        "created", ticket.getCreatedAt() != null && ticket.getCreatedAt() > from))
                .toList();
        return java.util.Map.of("now", now - 1, "changes", changes);
    }

    /** Internal notes are withheld from requesters. */
    private TicketResponse respond(Ticket ticket, Principal principal) {
        return TicketResponse.from(ticket, userService.isUsernameAssociatedWithElevatedAccount(principal.getName()));
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id, Principal principal) throws AccessDeniedException {
        return respond(ticketService.getTicketForUser(id, principal.getName()), principal);
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Principal principal
    ) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setDepartment(request.department());
        ticket.setRequestor(request.requestor());
        Ticket created = ticketService.createNewTicket(ticket, principal.getName());
        URI location = URI.create("/api/v1/tickets/" + created.getId());
        return ResponseEntity.created(location).body(respond(created, principal));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN', 'SYSADMIN')")
    public TicketResponse updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request,
            Principal principal
    ) throws AccessDeniedException {
        Ticket update = new Ticket();
        update.setTicketStatus(request.status());
        update.setImportance(request.importance());
        update.setDepartment(request.department());
        update.setAgentUsername(request.agentUsername());
        ticketService.updateTicket(update, id, principal.getName());
        return respond(ticketService.getTicketForUser(id, principal.getName()), principal);
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<TicketResponse> addNote(
            @PathVariable Long id,
            @Valid @RequestBody CreateNoteRequest request,
            Principal principal
    ) throws AccessDeniedException {
        ticketService.newNote(request.note(), principal.getName(), id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(respond(ticketService.getTicketForUser(id, principal.getName()), principal));
    }

    public record CreateTicketRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull TicketDepartment department,
            @NotBlank String requestor
    ) {
    }

    public record UpdateTicketRequest(
            @NotNull TicketStatus status,
            @NotNull TicketImportance importance,
            @NotNull TicketDepartment department,
            String agentUsername
    ) {
    }

    public record CreateNoteRequest(@NotBlank String note) {
    }

    public record TicketResponse(
            Long id,
            String title,
            String description,
            TicketDepartment department,
            String requestor,
            String requestorUsername,
            String agent,
            String agentUsername,
            TicketStatus status,
            TicketImportance importance,
            String ticketDate,
            String ticketTime,
            List<NoteResponse> notes
    ) {
        private static TicketResponse from(Ticket ticket, boolean includeInternal) {
            return new TicketResponse(
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getDepartment(),
                    ticket.getRequestor(),
                    ticket.getRequestorUsername(),
                    ticket.getAgent(),
                    ticket.getAgentUsername(),
                    ticket.getTicketStatus(),
                    ticket.getImportance(),
                    ticket.getTicketDate(),
                    ticket.getTicketTime(),
                    ticket.getNotes().stream()
                            .filter(note -> includeInternal || !note.isInternalNote())
                            .map(NoteResponse::from).toList()
            );
        }
    }

    public record NoteResponse(
            Long id,
            String username,
            String note,
            String date,
            String time
    ) {
        private static NoteResponse from(org.dev.helpdesk_pro.Data.Tickets.TicketNote note) {
            return new NoteResponse(
                    note.getId(),
                    note.getUsername(),
                    note.getNote(),
                    note.getTicketNoteDate(),
                    note.getTicketNoteTime()
            );
        }
    }
}
