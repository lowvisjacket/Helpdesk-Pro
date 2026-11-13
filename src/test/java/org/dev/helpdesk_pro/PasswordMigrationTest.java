package org.dev.helpdesk_pro;

import org.dev.helpdesk_pro.Data.Users.User;
import org.dev.helpdesk_pro.Data.Users.UserRepository;
import org.dev.helpdesk_pro.Enum.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordMigrationTest {

    private static final String RAW = "legacy-bcrypt-login-1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void newHashesUseArgon2id() {
        assertTrue(passwordEncoder.encode("a-brand-new-password").startsWith("{argon2}$argon2id$"));
    }

    @Test
    void legacyBcryptHashStillLogsInAndIsUpgradedToArgon2id() throws Exception {
        User legacy = new User();
        legacy.setFirstName("Legacy");
        legacy.setLastName("Account");
        legacy.setUsername("legacy.bcrypt");
        legacy.setDepartment("FINANCE");
        legacy.setRole(UserRole.USER);
        legacy.setAccountStatus(true);
        legacy.setLoginAttempts(0L);
        // Stored the old way: a bare bcrypt hash with no {id} prefix.
        legacy.setPassword(new BCryptPasswordEncoder().encode(RAW));
        userRepository.save(legacy);

        mockMvc.perform(post("/auth/login").with(csrf()).param("username", "legacy.bcrypt").param("password", RAW))
                .andExpect(redirectedUrl("/home"));

        String stored = userRepository.findByUsername("legacy.bcrypt").getPassword();
        assertTrue(stored.startsWith("{argon2}$argon2id$"), "hash should be upgraded on login");
        assertTrue(passwordEncoder.matches(RAW, stored));
        assertFalse(passwordEncoder.matches("wrong-password", stored));
        userRepository.delete(userRepository.findByUsername("legacy.bcrypt"));
    }
}
