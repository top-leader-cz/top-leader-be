package com.topleader.topleader.session.report;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportSessionViewRepository extends CrudRepository<ReportSessionView, Long>, PagingAndSortingRepository<ReportSessionView, Long> {

    List<ReportSessionView> findAllByCompanyId(Long companyId);

    @Query("""
        SELECT * FROM report_session_view
        WHERE company_id = :companyId
        AND (:username IS NULL OR username = :username)
        AND (:status IS NULL OR status = :status)
        AND (:from IS NULL OR date >= :from)
        AND (:to IS NULL OR date <= :to)
        ORDER BY date DESC
        """)
    List<ReportSessionView> findFiltered(Long companyId, String username, String status, LocalDateTime from, LocalDateTime to);
}
