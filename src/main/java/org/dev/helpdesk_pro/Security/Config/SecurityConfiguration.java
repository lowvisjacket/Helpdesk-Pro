package org.dev.helpdesk_pro.Security.Config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            org.dev.helpdesk_pro.Security.Config.AuthConfiguration authConfiguration,
            org.dev.helpdesk_pro.Security.Config.FailureConfiguration failureConfiguration
    ) throws Exception {
        RequestMatcher apiMatcher = request -> request.getServletPath().startsWith("/api/");
        AuthenticationEntryPoint apiAuthenticationEntryPoint = (request, response, exception) ->
                writeApiError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required");
        AccessDeniedHandler apiAccessDeniedHandler = (request, response, exception) ->
                writeApiError(response, HttpServletResponse.SC_FORBIDDEN, "Access is denied");

        http.authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/auth/login", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/actuator/health").permitAll()
                                .requestMatchers("/dashboard/**").hasAnyRole("TECHNICIAN", "ADMIN", "SYSADMIN")
                                .requestMatchers("/success/").authenticated()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/403").permitAll()
                                .requestMatchers("/540").permitAll()
                                .anyRequest().authenticated()
                        )
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(apiAuthenticationEntryPoint, apiMatcher)
                        .defaultAccessDeniedHandlerFor(apiAccessDeniedHandler, apiMatcher)
                )
                .formLogin((form) -> form
                        .loginPage("/auth/login").permitAll()
                        .successHandler(authConfiguration)
                        .failureHandler(failureConfiguration)
                )
                .logout(LogoutConfigurer::permitAll);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
        userDetailsManager.setUsersByUsernameQuery(
                "select username, password, accountStatus as enabled from users where username = ?"
        );
        userDetailsManager.setAuthoritiesByUsernameQuery(
                "select username, concat('ROLE_', role) from users where username = ?"
        );
        return userDetailsManager;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            org.dev.helpdesk_pro.Data.Users.UserRepository userRepository
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // After a successful login with an outdated hash (e.g. bcrypt), store the re-encoded Argon2id hash.
        provider.setUserDetailsPasswordService((user, newPassword) -> {
            org.dev.helpdesk_pro.Data.Users.User stored = userRepository.findByUsername(user.getUsername());
            if (stored != null) {
                stored.setPassword(newPassword);
                userRepository.save(stored);
            }
            return org.springframework.security.core.userdetails.User.withUserDetails(user).password(newPassword).build();
        });
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Argon2id for every new hash. Legacy hashes stored without an {id} prefix are bcrypt.
        java.util.Map<String, PasswordEncoder> encoders = new java.util.HashMap<>();
        encoders.put("argon2", org.springframework.security.crypto.argon2.Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        org.springframework.security.crypto.password.DelegatingPasswordEncoder encoder =
                new org.springframework.security.crypto.password.DelegatingPasswordEncoder("argon2", encoders);
        encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return encoder;
    }

    private static void writeApiError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().printf("{\"status\":%d,\"message\":\"%s\"}", status, message);
    }
}
