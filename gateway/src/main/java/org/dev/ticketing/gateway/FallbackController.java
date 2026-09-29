package org.dev.ticketing.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FallbackController {
    @GetMapping(path = "/fallback", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> backendUnavailable() {
        return Map.of(
                "status", "503",
                "error", "Service Unavailable",
                "message", "The ticketing service is temporarily unavailable."
        );
    }
}
