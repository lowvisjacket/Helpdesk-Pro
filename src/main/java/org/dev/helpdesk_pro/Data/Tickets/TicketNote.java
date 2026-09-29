package org.dev.ticketing_software.Data.Tickets;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneOffset;

@Entity
@Table(name = "ticketNotes")
@Getter
@Setter
public class TicketNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticketId")
    private Ticket ticket;

    private String username;

    @Column(columnDefinition = "TEXT")
    private String note;
    @Column(name = "ticketNoteDate")
    private String ticketNoteDate;
    @Column(name = "ticketNoteTime")
    private String ticketNoteTime;

    public TicketNote() {
    }

    public TicketNote(Ticket ticket, String note, String username) {
        Instant now = Instant.now();
        this.ticket = ticket;
        this.username = username;
        this.note = note;
        this.ticketNoteDate = now.atZone(ZoneOffset.UTC).toLocalDate().toString();
        this.ticketNoteTime = now.atZone(ZoneOffset.UTC).toLocalTime().toString();
    }
}
