package org.dev.ticketing_software.Data.Users;

import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.lang.annotation.Native;
import java.util.List;

public interface UserRepository extends CrudRepository<User, String> {
    User findByUsername(String username);

    @NativeQuery("select * from users where role != ?")
    List<User> findByNotRole(String role);
}
