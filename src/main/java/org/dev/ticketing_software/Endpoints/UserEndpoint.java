package org.dev.ticketing_software.Endpoints;

import org.dev.ticketing_software.Data.Users.User;
import org.dev.ticketing_software.Data.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/users")
@RestController
public class UserEndpoint {

    @Autowired
    private UserRepository userRepository;

    //retrieves user by uuid
    @GetMapping("/uuid/{uuid}")
    public Optional<User> getUserByUUID(@PathVariable String uuid) {
        return userRepository.findById(uuid);
    }

    //retrieves user by username
    @GetMapping("/username/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username);
    }
}
