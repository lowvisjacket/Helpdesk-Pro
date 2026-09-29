package org.dev.ticketing_software;

import org.dev.ticketing_software.TestData.UserSeeder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class TicketingSoftwareApplication {
//    @Autowired
//    static UserSeeder userSeeder;
    public static void main(String[] args) {
        SpringApplication.run(TicketingSoftwareApplication.class, args);
//        userSeeder.run();
    }

}
