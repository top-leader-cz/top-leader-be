package com.topleader.topleader.session.report;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportSessionViewRepository extends CrudRepository<ReportSessionView, Long>, PagingAndSortingRepository<ReportSessionView, Long> {


    @Query("""
        SELECT * FROM report_session_view
        WHERE company_id = :companyId
        AND (CAST(:username AS text) IS NULL OR username = :username)
        AND (CAST(:status AS text) IS NULL OR status = :status)
        AND (CAST(:from AS timestamp) IS NULL OR date IS NULL OR date >= :from)
        AND (CAST(:to AS timestamp) IS NULL OR date IS NULL OR date <= :to)
        ORDER BY date DESC NULLS LAST
        """)
    List<ReportSessionView> findFiltered(Long companyId, String username, String status, LocalDateTime from, LocalDateTime to);
}
