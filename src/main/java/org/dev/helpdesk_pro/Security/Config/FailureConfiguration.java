package org.dev.ticketing_software.Security.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dev.ticketing_software.Logic.UserService;
import org.dev.ticketing_software.Exceptions.UserNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FailureConfiguration implements AuthenticationFailureHandler {
    private final UserService userService;

    public FailureConfiguration(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        if (exception instanceof DisabledException) {
            redirectToDisabledAccountPage(request, response);
            return;
        } else if (exception instanceof BadCredentialsException && username != null) {
            try {
                userService.updateLoginAttempts(username);
            } catch (DisabledException disabledException) {
                redirectToDisabledAccountPage(request, response);
                return;
            } catch (UserNotFoundException ignored) {
                // Keep invalid usernames indistinguishable from incorrect passwords.
            }
        }
        response.sendRedirect("/auth/login?error=true");
    }

    private void redirectToDisabledAccountPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.getSession().setAttribute("accountDisabled", true);
        response.sendRedirect("/540");
    }
}
