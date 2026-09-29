package org.dev.ticketing_software.Security.Endpoints;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthEndpoints {

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }
}
