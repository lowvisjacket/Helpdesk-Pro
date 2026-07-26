package org.dev.ticketing_software.Data.Tickets;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    @Column(name = "department")
    private String department;
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
    @Column(name = "ticketStatus")
    private String ticketStatus;
    @Column(name = "importance")
    private String importance;
    @Column(name = "ticketDate")
    private String ticketDate;
    @Column(name = "ticketTime")
    private String ticketTime;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TicketNote> notes = new ArrayList<>();

    public Ticket() {}

    public Ticket(String h, String b, String d, String r, String di) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.department = d;
        this.requestorUsername = di;
        this.title = h;
        this.description = b;
        this.requestor = r;
        this.agent = "";
        this.agentUsername = "";
        this.importance = "LOW";
        this.ticketStatus = "OPEN";
        this.ticketDate = LocalDate.now(ZoneOffset.UTC).format(formatter);
        this.ticketTime = LocalTime.now(ZoneOffset.UTC).toString();
        this.notes = new ArrayList<>();
    }

    public Ticket(String h, String b, String d, String r, String di, String a) {
        this.department = d;
        this.requestorUsername = di;
        this.title = h;
        this.description = b;
        this.requestor = r;
        this.agent = a;
        this.notes = new ArrayList<>();
    }

    //Enums
    private enum status {
       OPEN,
       CLOSED,
       PENDING_USER_RESPONSE,
       REQUESTOR_RESPONDED,
       PAUSED,
       IN_PROGRESS
    }

    private enum importance {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}