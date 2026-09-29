package org.dev.ticketing_software.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorEndpoint {

    @RequestMapping("/403")
    public String unauthorized() {
        return "403";
    }

    @GetMapping("/540")
    public String getDisabledScreen(HttpSession session) {
        Boolean disabled = (Boolean) session.getAttribute("accountDisabled");
        if (Boolean.TRUE.equals(disabled)) {
            session.removeAttribute("accountDisabled");
            return "disabledAccount";
        } else {
            return "redirect:/auth/login";
        }
    }
}
