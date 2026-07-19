package org.dev.ticketing_software.Data.Tickets;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Entity(name = "tickets")
@Getter
@Setter
public class Ticket implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String department;
    private String requestor;
    private int requestor_id;
    private String title;
    private String description;
    private String agent;
    private Integer agent_id;
    private String ticket_status;
    private String importance;
    private String ticket_date;
    private String ticket_time;

    public Ticket() {}

    public Ticket(String h, String b, String d, String r, Integer di) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.department = d;
        this.requestor_id = di;
        this.title = h;
        this.description = b;
        this.requestor = r;
        this.agent = "";
        this.agent_id = 0000;
        this.importance = "LOW";
        this.ticket_status = "OPEN";
        this.ticket_date = LocalDate.now(ZoneOffset.UTC).format(formatter);
        this.ticket_time = LocalTime.now(ZoneOffset.UTC).toString();
    }

    public Ticket(String h, String b, String d, String r, int di, String a) {
        this.department = d;
        this.requestor_id = di;
        this.title = h;
        this.description = b;
        this.requestor = r;
        this.agent = a;
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