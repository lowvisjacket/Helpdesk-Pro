package org.dev.ticketing_software.TestData;

import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.dev.ticketing_software.Enum.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Component
@Profile("demo")
public class UserSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(UserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedPassword;
    private final int seedCount;

    public UserSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${demo.seed.password:test123}") String seedPassword,
                      @Value("${demo.seed.count:300}") int seedCount) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedPassword = seedPassword;
        this.seedCount = seedCount;
    }

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) {
            return;
        }

        String[] firstNames = {
                "James", "Mary", "John", "Patricia", "Robert",
                "Jennifer", "Michael", "Linda", "William", "Elizabeth",
                "David", "Barbara", "Richard", "Susan", "Joseph",
                "Jessica", "Thomas", "Sarah", "Charles", "Karen"
        };

        String[] lastNames = {
                "Smith", "Johnson", "Williams", "Brown", "Jones",
                "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
                "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
                "Thomas", "Taylor", "Moore", "Jackson", "Martin"
        };

        String[] departments = {
                "INFORMATION_TECHNOLOGY",
                "HUMAN_RESOURCES",
                "ENGINEERING",
                "MAINTENANCE",
                "CLINICAL_INFORMATICS",
                "SECURITY"
        };




        String[] roles = {
                "USER",
                "TECHNICIAN",
                "ADMIN",
                "SYSADMIN"
        };

        Set<String> usedNames = new HashSet<>();

        int created = 0;
        int itCount = 0;
        int sysAdminCount = 0;
        int adminCount = 0;

        for (String firstName : firstNames) {

            for (String lastName : lastNames) {

                if (created >= seedCount) {
                    break;
                }

                String fullName = firstName + " " + lastName;

                // Prevent duplicate names
                if (usedNames.contains(fullName)) {
                    continue;
                }

                usedNames.add(fullName);


                User user = new User();

                user.setFirstName(firstName);
                user.setLastName(lastName);

                String username = (
                        firstName.substring(0, 1) +
                                lastName.toLowerCase() +
                                String.format("%03d", created + 1)
                ).toLowerCase();

                user.setUsername(username);


                // Same password, different BCrypt salt per user
                user.setPassword(
                        passwordEncoder.encode(seedPassword)
                );


                String department = departments[created % departments.length];


                if (department.equals("INFORMATION_TECHNOLOGY") && itCount < 20) {

                    itCount++;

                    user.setDepartment("INFORMATION_TECHNOLOGY");


                    // Assign IT roles
                    if (sysAdminCount < 4) {

                        user.setRole(UserRole.SYSADMIN);
                        sysAdminCount++;

                    } else if (adminCount < 6) {

                        user.setRole(UserRole.ADMIN);
                        adminCount++;

                    } else {
                        user.setRole(UserRole.TECHNICIAN);
                    }


                } else {
                    user.setDepartment(department);
                    user.setRole(UserRole.USER);
                }


                user.setAccountStatus(true);
                user.setLoginAttempts(0L);

                userRepository.save(user);

                created++;
            }

            if (created >= seedCount) {
                break;
            }
        }


        logger.info("Created {} demo users", created);
    }
}