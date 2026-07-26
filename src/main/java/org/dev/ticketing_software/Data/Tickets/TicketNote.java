package org.dev.ticketing_software.Data.Tickets;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    private LocalDateTime createdAt;

    public TicketNote() {}
}
