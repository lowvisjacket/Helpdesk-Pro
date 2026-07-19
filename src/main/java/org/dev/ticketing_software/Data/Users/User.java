package org.dev.ticketing_software.Data.Users;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity(name = "users")
@Getter
@Setter
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private String uuid;
    private String first_name;
    private String last_name;
    private String username;
    private String password;
    private String department;
    private String role;
    private boolean account_status;
}
