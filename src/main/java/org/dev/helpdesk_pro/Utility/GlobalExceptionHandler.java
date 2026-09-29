package org.dev.ticketing_software.Utility;

import org.dev.ticketing_software.Exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.dev.ticketing_software.Exceptions.TicketException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TicketException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleTicketException(TicketException ticketException) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ticketException.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleUserNotFoundException(UserNotFoundException userNotFoundException) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), userNotFoundException.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleAccessDeniedException(AccessDeniedException accessDeniedException) {
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), accessDeniedException.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException illegalArgumentException) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), illegalArgumentException.getMessage());
    }
}
