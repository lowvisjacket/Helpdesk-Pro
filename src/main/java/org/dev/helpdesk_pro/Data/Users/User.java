package org.dev.ticketing_software.Data.Users;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.dev.ticketing_software.Enum.UserRole;
import org.hibernate.annotations.UuidGenerator;

@Entity(name = "users")
@Getter
@Setter
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private String uuid;

    private String firstName;

    private String lastName;

    private String username;

    private String password;

    private String department;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean accountStatus;

    private Long loginAttempts;
}
