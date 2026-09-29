package org.dev.ticketing_software.Logic;

import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.dev.ticketing_software.Enum.UserRole;
import org.dev.ticketing_software.Exceptions.UserException;
import org.dev.ticketing_software.Exceptions.UserNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByUsername(String username) {
        validateIfUserExists(username);
        return userRepository.findByUsername(username);
    }

    protected void validateIfUserExists(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User does not exist: " + username);
        }
    }

    protected void validateAgentUsername(String username) {
        User user = getUserByUsername(username);
        if (user.getRole() == UserRole.USER) {
            throw new UserException("User is not an agent");
        }
    }

    protected String getAgentName(String username) {
        validateAgentUsername(username); //make sure the user is actually an agent before returning their full name for ticket reference
        User user = getUserByUsername(username);

        return user.getFirstName() + " " + //build their full name from 2 separate fields
                user.getLastName();
    }

    protected UserRole getUserRole(String username) {
        return getUserByUsername(username).getRole();
    }

    public boolean isUsernameAssociatedWithElevatedAccount(String username) {
        return !getUserRole(username).equals(UserRole.USER);
    }

    public void updateLoginAttempts(String username) {
        User user = getUserByUsername(username);
        long loginAttempts = user.getLoginAttempts() == null ? 0 : user.getLoginAttempts();
        if (loginAttempts >= 3) {
            user.setAccountStatus(false);
            userRepository.save(user);
            throw new DisabledException("User Account is disabled");
        }
        user.setLoginAttempts(loginAttempts + 1);
        userRepository.save(user);
    }
}
