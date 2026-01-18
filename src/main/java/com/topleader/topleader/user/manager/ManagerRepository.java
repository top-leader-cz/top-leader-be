package com.topleader.topleader.user.manager;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    List<Manager> findByCompanyId(long companyId);

    Optional<Manager> findByUsername(String username);

}
