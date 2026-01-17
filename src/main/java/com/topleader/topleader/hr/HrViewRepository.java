package com.topleader.topleader.hr;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface HrViewRepository extends CrudRepository<HrView, String> {

    List<HrView> findAllByCompanyId(Long companyId);
}
