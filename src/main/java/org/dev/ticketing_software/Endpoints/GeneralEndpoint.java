package org.dev.ticketing_software.Endpoints;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GeneralEndpoint {

    @GetMapping("/success/")
    public String getSuccessTicket() {
        return "success";
    }
}
