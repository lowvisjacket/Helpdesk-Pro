package org.dev.ticketing_software.Endpoints;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorEndpoint {

//    @RequestMapping("/error")
//    public String getGenericErrorPage() {
//        return "error";
//    }

    @RequestMapping("/403")
    public String unauthorized() {
        return "403";
    }
}
