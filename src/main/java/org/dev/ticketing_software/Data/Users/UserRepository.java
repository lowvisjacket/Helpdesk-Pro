package org.dev.ticketing_software.Data.Users;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, String> {
    User findByUsername(String username);
}
