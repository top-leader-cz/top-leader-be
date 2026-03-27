package com.topleader.topleader.session.coaching_package;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface CoachingPackageRepository extends ListCrudRepository<CoachingPackage, Long> {

    List<CoachingPackage> findByCompanyId(Long companyId);

}
