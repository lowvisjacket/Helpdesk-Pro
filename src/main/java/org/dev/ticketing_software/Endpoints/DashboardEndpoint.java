package org.dev.ticketing_software.Endpoints;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardEndpoint {

    @GetMapping("/")
    public String getDashboard() {
        return "dashboard";
    }
}
