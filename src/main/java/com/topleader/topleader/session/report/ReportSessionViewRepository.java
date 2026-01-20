package com.topleader.topleader.session.report;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportSessionViewRepository extends ListCrudRepository<ReportSessionView, Long>,
                                                       PagingAndSortingRepository<ReportSessionView, Long> {

    @Query("""
        SELECT * FROM report_session_view
        WHERE company_id = :companyId
        AND (CAST(:fromDate AS timestamp) IS NULL OR date IS NULL OR date >= :fromDate)
        """)
    List<ReportSessionView> findFiltered(long companyId, LocalDateTime fromDate);
}
