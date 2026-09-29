package org.dev.ticketing_software.Data.Tickets;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dev.ticketing_software.Enum.TicketDepartment;
import org.dev.ticketing_software.Enum.TicketImportance;
import org.dev.ticketing_software.Enum.TicketStatus;

import java.io.Serializable;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    private TicketDepartment department;

    @Column(name = "requestor")
    private String requestor;

    @Column(name = "requestorUsername")
    private String requestorUsername;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "agent")
    private String agent;

    @Column(name = "agentUsername")
    private String agentUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticketStatus")
    private TicketStatus ticketStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance")
    private TicketImportance importance;

    @Column(name = "ticketDate")
    private String ticketDate;

    @Column(name = "ticketTime")
    private String ticketTime;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ticketNoteDate ASC, ticketNoteTime ASC")
    private List<TicketNote> notes = new ArrayList<>();

    public Ticket() {
        this.notes = new ArrayList<>();
    }
    public Ticket(String title, String description, TicketDepartment department, String requestor, String requestorUsername) {
        Instant now = Instant.now();
        this.department = department;
        this.requestorUsername = requestorUsername;
        this.title = title;
        this.description = description;
        this.requestor = requestor;
        this.agent = null;
        this.agentUsername = null;
        this.importance = TicketImportance.LOW;
        this.ticketStatus = TicketStatus.OPEN;
        this.ticketDate = now.atZone(ZoneOffset.UTC).toLocalDate().toString();
        this.ticketTime = now.atZone(ZoneOffset.UTC).toLocalTime().toString();
        this.notes = new ArrayList<>();
    }
}