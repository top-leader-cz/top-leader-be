/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachingPackageRepository extends JpaRepository<CoachingPackage, Long> {

    List<CoachingPackage> findByCompanyId(Long companyId);

}
