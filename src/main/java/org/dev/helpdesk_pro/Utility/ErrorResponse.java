package org.dev.ticketing_software.Utility;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final int status;
    private final String message;
    public ErrorResponse(int s, String m) {
        this.status = s;
        this.message = m;
    }
}
