package com.topleader.topleader.hr;

import com.topleader.topleader.admin.AdminView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrViewRepository extends JpaRepository<HrView, String> {

    List<HrView> findAllByCompanyId(Long companyId);
}
