package com.topleader.topleader.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;


public interface AdminViewRepository extends ListCrudRepository<AdminView, Long> {

    @Query("""
        SELECT * FROM admin_view
        WHERE (COALESCE(:username, '') = '' OR username = :username)
        AND (COALESCE(:firstName, '') = '' OR first_name ILIKE '%' || :firstName || '%')
        AND (COALESCE(:lastName, '') = '' OR last_name = :lastName)
        AND (COALESCE(:timeZone, '') = '' OR time_zone = :timeZone)
        AND (COALESCE(:status, '') = '' OR status = :status)
        AND (CAST(:companyId AS bigint) IS NULL OR company_id = :companyId)
        AND (COALESCE(:companyName, '') = '' OR company_name ILIKE '%' || :companyName || '%')
        AND (COALESCE(:coach, '') = '' OR coach ILIKE '%' || :coach || '%')
        AND (COALESCE(:coachFirstName, '') = '' OR coach_first_name ILIKE '%' || :coachFirstName || '%')
        AND (COALESCE(:coachLastName, '') = '' OR coach_last_name ILIKE '%' || :coachLastName || '%')
        AND (CAST(:credit AS integer) IS NULL OR credit = :credit)
        AND (CAST(:requestedCredit AS integer) IS NULL OR requested_credit = :requestedCredit)
        AND (CAST(:sumRequestedCredit AS integer) IS NULL OR sum_requested_credit = :sumRequestedCredit)
        AND (CAST(:paidCredit AS integer) IS NULL OR paid_credit = :paidCredit)
        AND (COALESCE(:hrs, '') = '' OR hrs ILIKE '%' || :hrs || '%')
        AND (COALESCE(:requestedBy, '') = '' OR requested_by ILIKE '%' || :requestedBy || '%')
        AND (COALESCE(:freeCoach, '') = '' OR free_coach = :freeCoach)
        AND (COALESCE(:maxCoachRate, '') = '' OR allowed_coach_rates ILIKE '%' || :maxCoachRate || '%')
        AND (CAST(:showCanceled AS boolean) IS TRUE OR status != 'CANCELED')
        """)
    List<AdminView> findFiltered(
            String username,
            String firstName,
            String lastName,
            String timeZone,
            String status,
            Long companyId,
            String companyName,
            String coach,
            String coachFirstName,
            String coachLastName,
            Integer credit,
            Integer requestedCredit,
            Integer sumRequestedCredit,
            Integer paidCredit,
            String hrs,
            String requestedBy,
            String freeCoach,
            String maxCoachRate,
            Boolean showCanceled,
            Pageable pageable
    );
}
