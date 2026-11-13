package org.dev.helpdesk_pro.Controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorEndpoint {

    @RequestMapping("/403")
    public String unauthorized(Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("status", 403);
        return "error";
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
