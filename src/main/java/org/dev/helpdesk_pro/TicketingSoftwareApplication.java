package org.dev.helpdesk_pro;

import org.dev.helpdesk_pro.TestData.UserSeeder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class TicketingSoftwareApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingSoftwareApplication.class, args);
    }

}
