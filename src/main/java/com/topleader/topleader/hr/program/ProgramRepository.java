package com.topleader.topleader.hr.program;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends ListCrudRepository<Program, Long> {

    @Query("""
            SELECT
                p.id,
                p.name,
                cp.status,
                cp.valid_from,
                cp.valid_to,
                p.milestone_date,
                COUNT(ua.id)                                              AS total_participants,
                COUNT(ua.id) FILTER (WHERE ua.status = 'ACTIVE')         AS active_participants
            FROM program p
            JOIN coaching_package cp ON cp.id = p.coaching_package_id
            LEFT JOIN user_allocation ua ON ua.package_id = cp.id
            WHERE cp.company_id = :companyId
            GROUP BY p.id, p.name, cp.status, cp.valid_from, cp.valid_to, p.milestone_date
            ORDER BY p.name
            """)
    List<ProgramSummaryRow> findSummariesByCompanyId(Long companyId);

    @Query("""
            SELECT
                p.id,
                p.name,
                p.coaching_package_id,
                cp.status,
                cp.valid_from,
                cp.valid_to,
                p.milestone_date
            FROM program p
            JOIN coaching_package cp ON cp.id = p.coaching_package_id
            WHERE p.id = :programId
              AND cp.company_id = :companyId
            """)
    Optional<ProgramRow> findByIdAndCompanyId(Long programId, Long companyId);

    @Query("""
            SELECT
                u.username,
                u.first_name,
                u.last_name,
                u.last_login_at,
                COALESCE(pp.coach_username, u.coach) AS coach_username,
                ua.consumed_units,
                ua.allocated_units
            FROM program p
            JOIN coaching_package cp ON cp.id = p.coaching_package_id
            JOIN user_allocation ua ON ua.package_id = cp.id
            JOIN users u ON u.username = ua.username
            LEFT JOIN program_participant pp
                ON pp.program_id = p.id AND pp.username = ua.username
            WHERE p.id = :programId
            ORDER BY u.last_name, u.first_name
            """)
    List<ParticipantRow> findParticipants(Long programId);

    record ProgramSummaryRow(
            Long id,
            String name,
            String status,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            LocalDateTime milestoneDate,
            int totalParticipants,
            int activeParticipants
    ) {}

    record ProgramRow(
            Long id,
            String name,
            Long coachingPackageId,
            String status,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            LocalDateTime milestoneDate
    ) {}

    @Query("""
            SELECT
                u.username,
                u.first_name,
                u.last_name,
                u.email,
                u.coach,
                CASE WHEN pp.id IS NOT NULL THEN true ELSE false END AS added
            FROM users u
            LEFT JOIN program_participant pp ON pp.username = u.username
                AND (CAST(:programId AS bigint) IS NOT NULL AND pp.program_id = :programId)
            WHERE u.company_id = :companyId
              AND u.status != 'CANCELED'
              AND u.authorities::text LIKE '%USER%'
            ORDER BY u.last_name, u.first_name
            """)
    List<CompanyUserRow> findCompanyUsersWithParticipation(Long programId, Long companyId);

    record ParticipantRow(
            String username,
            String firstName,
            String lastName,
            LocalDateTime lastLoginAt,
            String coachUsername,
            int consumedUnits,
            int allocatedUnits
    ) {}

    record CompanyUserRow(
            String username,
            String firstName,
            String lastName,
            String email,
            String coach,
            boolean added
    ) {}
}
