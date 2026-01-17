/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CoachingPackageRepository extends CrudRepository<CoachingPackage, Long>, PagingAndSortingRepository<CoachingPackage, Long> {

    List<CoachingPackage> findByCompanyId(Long companyId);

}
