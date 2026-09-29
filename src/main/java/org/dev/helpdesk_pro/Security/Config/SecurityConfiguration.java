package org.dev.ticketing_software.Security.Config;

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
            AuthConfiguration authConfiguration,
            FailureConfiguration failureConfiguration
    ) throws Exception {
        RequestMatcher apiMatcher = request -> request.getServletPath().startsWith("/api/");
        AuthenticationEntryPoint apiAuthenticationEntryPoint = (request, response, exception) ->
                writeApiError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required");
        AccessDeniedHandler apiAccessDeniedHandler = (request, response, exception) ->
                writeApiError(response, HttpServletResponse.SC_FORBIDDEN, "Access is denied");

        http.authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/auth/login", "/css/**", "/images/**", "/favicon.ico", "/actuator/health").permitAll()
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
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
