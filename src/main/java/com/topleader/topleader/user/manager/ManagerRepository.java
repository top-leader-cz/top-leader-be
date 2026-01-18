package com.topleader.topleader.user.manager;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface ManagerRepository extends ListCrudRepository<Manager, Long> {

    @Query("SELECT * FROM manager_view WHERE company_id = :companyId")
    List<Manager> findByCompanyId(long companyId);

    @Query("SELECT * FROM manager_view WHERE username = :username")
    Optional<Manager> findByUsername(String username);

}
