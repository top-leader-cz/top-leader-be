package com.topleader.topleader.hr;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface HrViewRepository extends ListCrudRepository<HrView, String> {

    List<HrView> findByCompanyId(Long companyId);
}
