package com.topleader.topleader.user.manager;

import com.topleader.topleader.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagerRepository extends JpaRepository<Manager, String> {

    List<Manager> findByCompanyId(long companyId);

}
