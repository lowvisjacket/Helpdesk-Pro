package org.dev.helpdesk_pro.Utility;

import jakarta.servlet.http.HttpServletRequest;
import org.dev.helpdesk_pro.Exceptions.TicketException;
import org.dev.helpdesk_pro.Exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.AccessDeniedException;

/** API requests get a JSON error body; page requests get the styled error page. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({TicketException.class, UserNotFoundException.class})
    public Object handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return respond(request, HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        return respond(request, HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        return respond(request, HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolation(HttpServletRequest request) {
        return respond(request, HttpStatus.BAD_REQUEST, "One of the values is too long or is already in use. Shorten it and try again.");
    }

    private Object respond(HttpServletRequest request, HttpStatus status, String message) {
        if (request.getServletPath().startsWith("/api/")) {
            return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
        }
        ModelAndView page = new ModelAndView("error");
        page.setStatus(status);
        page.addObject("status", status.value());
        // Only validation messages are shown; they are written for the person filling in the form.
        if (status == HttpStatus.BAD_REQUEST) {
            page.addObject("detail", message);
        }
        return page;
    }
}
