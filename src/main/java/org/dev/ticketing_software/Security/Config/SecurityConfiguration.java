package org.dev.ticketing_software.Security.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class SecurityConfiguration {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/home").authenticated()
                                .requestMatchers("/auth/login").permitAll()
                                .requestMatchers("/dashboard/**").hasAnyAuthority("TECHNICIAN", "ADMIN", "SYSADMIN")
                                .anyRequest().authenticated()
                        )
                .formLogin((form) -> form
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/dashboard/", true)
                )
                .logout(LogoutConfigurer::permitAll);
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("select username, password, account_status as enabled " + "from users " + "where username = ?")
                .authoritiesByUsernameQuery("select username, role " + "from users " + "where username = ?");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
