
package com.topleader.topleader.user.userinfo;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;


/**
 * @author Daniel Slavik
 */
public interface UserInfoRepository extends ListCrudRepository<UserInfo, Long> {

    @Query("SELECT * FROM user_info WHERE username = :username")
    Optional<UserInfo> findByUsername(String username);
}
