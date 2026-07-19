//package org.dev.ticketing_software.TestData;
//
//import org.dev.ticketing_software.Data.Users.User;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Component
//public class UserSeeder implements CommandLineRunner {
//
//    private final org.dev.ticketing_software.UserRepository2 userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public UserSeeder(org.dev.ticketing_software.UserRepository2 userRepository,
//                      PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public void run(String... args) {
//
//        if (userRepository.count() > 0) {
//            return;
//        }
//
//        String[] firstNames = {
//                "James", "Mary", "John", "Patricia", "Robert",
//                "Jennifer", "Michael", "Linda", "William", "Elizabeth",
//                "David", "Barbara", "Richard", "Susan", "Joseph",
//                "Jessica", "Thomas", "Sarah", "Charles", "Karen"
//        };
//
//        String[] lastNames = {
//                "Smith", "Johnson", "Williams", "Brown", "Jones",
//                "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
//                "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
//                "Thomas", "Taylor", "Moore", "Jackson", "Martin"
//        };
//
//        String[] departments = {
//                "IT",
//                "Engineering",
//                "Finance",
//                "Human Resources",
//                "Marketing",
//                "Sales",
//                "Operations",
//                "Maintenance",
//                "Heath",
//                "Internal Affairs"
//        };
//
//
//
//
//        String[] roles = {
//                "USER",
//                "TECHNICIAN",
//                "ADMIN",
//                "SYSADMIN"
//        };
//
//        String[] statuses = {
//                "ACTIVE",
//                "INACTIVE",
//                "DISABLED"
//        };
//
//
//        Set<String> usedNames = new HashSet<>();
//
//        int created = 0;
//        int itCount = 0;
//        int sysAdminCount = 0;
//        int adminCount = 0;
//
//        for (String firstName : firstNames) {
//
//            for (String lastName : lastNames) {
//
//                if (created >= 300) {
//                    break;
//                }
//
//                String fullName = firstName + " " + lastName;
//
//                // Prevent duplicate names
//                if (usedNames.contains(fullName)) {
//                    continue;
//                }
//
//                usedNames.add(fullName);
//
//
//                User user = new User();
//
//                user.setFirst_name(firstName);
//                user.setLast_name(lastName);
//
//                String username = (
//                        firstName.substring(0, 1) +
//                                lastName.toLowerCase() +
//                                String.format("%03d", created + 1)
//                ).toLowerCase();
//
//                user.setUsername(username);
//
//
//                // Same password, different BCrypt salt per user
//                user.setPassword(
//                        passwordEncoder.encode("test123")
//                );
//
//
//                String department = departments[created % departments.length];
//
//
//                // Limit IT users to 12
//                if (department.equals("IT") && itCount < 12) {
//
//                    itCount++;
//
//                    user.setDepartment("IT");
//
//
//                    // Assign IT roles
//                    if (sysAdminCount < 2) {
//
//                        user.setRole("SYSADMIN");
//                        sysAdminCount++;
//
//                    } else if (adminCount < 2) {
//
//                        user.setRole("ADMIN");
//                        adminCount++;
//
//                    } else {
//
//                        user.setRole("TECHNICIAN");
//                    }
//
//
//                } else {
//
//                    // Non-IT users cannot have elevated roles
//                    if (department.equals("IT")) {
//                        department = "Engineering";
//                    }
//
//                    user.setDepartment(department);
//                    user.setRole("USER");
//                }
//
//
//                user.setAccount_status(true);
//
//
//                userRepository.save(user);
//
//                created++;
//            }
//
//            if (created >= 300) {
//                break;
//            }
//        }
//
//
//        System.out.println("Created " + created + " unique users");
//    }
//}