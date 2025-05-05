package com.topleader.topleader.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportSessionViewRepository extends JpaRepository<ReportSessionView, String>, JpaSpecificationExecutor<ReportSessionView> {
}
