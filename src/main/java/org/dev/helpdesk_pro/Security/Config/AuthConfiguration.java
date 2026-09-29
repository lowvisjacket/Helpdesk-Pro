package org.dev.ticketing_software.Security.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class AuthConfiguration implements AuthenticationSuccessHandler {

    @Autowired
    UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        boolean accountStatus = userRepository.findByUsername(authentication.getName()).isAccountStatus();
        if (!accountStatus) {
            response.sendRedirect("/540");
            return;
        }
        var user = userRepository.findByUsername(authentication.getName());
        if (user.getLoginAttempts() != null && user.getLoginAttempts() > 0) {
            user.setLoginAttempts(0L);
            userRepository.save(user);
        }
        if (roles.contains("ROLE_USER")) {
            response.sendRedirect("/home");
        } else {
            response.sendRedirect("/dashboard/");
        }
    }
}
