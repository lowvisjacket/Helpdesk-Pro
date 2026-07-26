package org.dev.ticketing_software.Data.Users;

import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, String> {
    User findByUsername(String username);

    @NativeQuery("select * from users where role != ?")
    List<User> findByNotRole(String role);

    @NativeQuery("select * from users where accountStatus = 0")
    List<User> findDisabledAccountsCount();

    @NativeQuery("select * from users where accountStatus = 1")
    List<User> findEnabledAccountsCount();

    @Query("""
    SELECT t.requestorUsername, COUNT(t)
    FROM Ticket t
    GROUP BY t.requestorUsername
    ORDER BY COUNT(t) DESC
    """)
    List<Object[]> findTopTicketSubmitters();
}
