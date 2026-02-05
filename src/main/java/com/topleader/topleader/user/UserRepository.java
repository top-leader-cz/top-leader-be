package com.topleader.topleader.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface UserRepository extends ListCrudRepository<User, Long> {

     Optional<User> findByEmail(String email);

     Optional<User> findByUsername(String username);

     @Query("SELECT * FROM users WHERE username IN (:usernames)")
     List<User> findAllByUsernameIn(Collection<String> usernames);

     @Query("SELECT rate_name FROM user_coach_rates WHERE username = :username")
     Set<String> findAllowedCoachRates(String username);

     @Modifying
     @Query("DELETE FROM user_coach_rates WHERE username = :username")
     void deleteAllowedCoachRates(String username);

     @Modifying
     @Query("INSERT INTO user_coach_rates (username, rate_name) VALUES (:username, :rateName)")
     void insertAllowedCoachRate(String username, String rateName);

     @Query("SELECT * FROM users WHERE username = :username OR email = :username")
     Optional<User> findByUsernameOrEmail(String username);

     @Query("""
             SELECT * FROM users u
             WHERE u.company_id = :companyId
               AND (
                   u.status != 'CANCELED'
                   OR EXISTS (
                       SELECT 1 FROM user_allocation ua
                       WHERE ua.username = u.username
                         AND ua.allocated_units > 0
                   )
               )
             """)
     List<User> findActiveByCompanyId(Long companyId);

}
