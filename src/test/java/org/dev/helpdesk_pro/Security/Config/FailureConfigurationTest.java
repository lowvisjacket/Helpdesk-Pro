package org.dev.ticketing_software.Security.Config;

import org.dev.ticketing_software.Logic.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FailureConfigurationTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private FailureConfiguration failureConfiguration;

    @Test
    void incorrectPasswordRecordsAttemptAndReturnsToLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "alex");
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureConfiguration.onAuthenticationFailure(
                request,
                response,
                new BadCredentialsException("Incorrect password")
        );

        verify(userService).updateLoginAttempts("alex");
        assertEquals("/auth/login?error=true", response.getRedirectedUrl());
    }

    @Test
    void disabledAccountIsRedirectedToTheDisabledAccountPage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "alex");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new DisabledException("Account disabled"))
                .when(userService).updateLoginAttempts("alex");

        failureConfiguration.onAuthenticationFailure(
                request,
                response,
                new BadCredentialsException("Incorrect password")
        );

        assertEquals("/540", response.getRedirectedUrl());
        assertTrue((Boolean) request.getSession().getAttribute("accountDisabled"));
    }
}