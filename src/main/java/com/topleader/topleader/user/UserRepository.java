package com.topleader.topleader.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    List<User> findAllByCompanyId(Long companyId);
}
