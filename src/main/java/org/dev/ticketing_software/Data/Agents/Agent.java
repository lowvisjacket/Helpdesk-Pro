package org.dev.ticketing_software.Data.Agents;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "agents")
@Getter
@Setter
public class Agent {
    @Id
    private int id;
    private String first_name;
    private String last_name;
    private String role;
    private int department_id;
    private String department;
}
