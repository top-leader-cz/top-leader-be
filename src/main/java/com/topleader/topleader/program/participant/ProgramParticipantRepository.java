package com.topleader.topleader.program.participant;

import com.topleader.topleader.program.enrollment.PendingEnrollmentEmailRow;
import com.topleader.topleader.program.manager.ManagerParticipantRow;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProgramParticipantRepository extends ListCrudRepository<ProgramParticipant, Long> {

    List<ProgramParticipant> findByProgramId(Long programId);

    @Query("""
            SELECT pp.* FROM program_participant pp
            JOIN program p ON p.id = pp.program_id
            WHERE pp.username = :username
              AND p.status IN ('CREATED', 'ACTIVE')
            ORDER BY p.created_at DESC
            LIMIT 1
            """)
    Optional<ProgramParticipant> findActiveByUsername(String username);

    @Query("SELECT * FROM program_participant WHERE program_id = :programId AND username = :username")
    Optional<ProgramParticipant> findByProgramIdAndUsername(Long programId, String username);

    @Modifying
    @Query("DELETE FROM program_participant WHERE program_id = :programId AND username = :username")
    void deleteByProgramIdAndUsername(Long programId, String username);

    @Query("""
            SELECT pp.id AS participant_id,
                   pp.program_id,
                   pp.username,
                   pp.new_user,
                   u.first_name,
                   u.last_name,
                   u.email,
                   u.locale,
                   p.name AS program_name,
                   p.goal AS program_goal,
                   p.duration_days,
                   p.sessions_per_participant,
                   p.created_by AS hr_username,
                   cp.valid_from,
                   hr.first_name AS hr_first_name,
                   hr.last_name AS hr_last_name,
                   hr.email AS hr_email
            FROM program_participant pp
            JOIN program p ON p.id = pp.program_id
            JOIN coaching_package cp ON cp.id = p.coaching_package_id
            JOIN users u ON u.username = pp.username
            LEFT JOIN users hr ON hr.username = p.created_by
            WHERE pp.enrollment_email_scheduled_at <= :now
              AND pp.enrollment_email_sent_at IS NULL
              AND p.status IN ('CREATED', 'ACTIVE')
            """)
    List<PendingEnrollmentEmailRow> findPendingEnrollmentEmails(LocalDateTime now);

    @Query("""
            SELECT pp.username
            FROM program_participant pp
            JOIN program p ON p.id = pp.program_id
            WHERE pp.username IN (:usernames)
              AND p.status IN ('CREATED', 'ACTIVE')
              AND (CAST(:excludeProgramId AS bigint) IS NULL OR pp.program_id != :excludeProgramId)
            """)
    List<String> findUsernamesInActivePrograms(Collection<String> usernames, Long excludeProgramId);

    @Query("""
            SELECT
                pp.id AS participant_id,
                pp.focus_area,
                pp.personal_goal,
                pr.name AS program_name,
                LEAST(:cycle, pp.current_cycle) >= GREATEST(1, COALESCE(pr.duration_days, 90) / GREATEST(1, COALESCE(NULLIF(pr.cycle_length_days, 0), pr.duration_days, 90))) AS is_final_cycle,
                bl.q1 AS baseline_q1, bl.q2 AS baseline_q2, bl.q3 AS baseline_q3, bl.q4 AS baseline_q4, bl.q5 AS baseline_q5,
                md.q1 AS mid_q1, md.q2 AS mid_q2, md.q3 AS mid_q3, md.q4 AS mid_q4, md.q5 AS mid_q5,
                fn.q1 AS final_q1, fn.q2 AS final_q2, fn.q3 AS final_q3, fn.q4 AS final_q4, fn.q5 AS final_q5,
                ua.allocated_units,
                ua.consumed_units,
                COALESCE((SELECT COUNT(*) FROM weekly_practice wp WHERE wp.participant_id = pp.id AND wp.cycle = LEAST(:cycle, pp.current_cycle)), 0) AS practices_total,
                COALESCE((SELECT COUNT(*) FROM weekly_practice wp WHERE wp.participant_id = pp.id AND wp.cycle = LEAST(:cycle, pp.current_cycle) AND wp.friday_response IS NOT NULL), 0) AS practices_responded
            FROM program_participant pp
            JOIN program pr ON pr.id = pp.program_id
            JOIN assessment_response bl ON bl.participant_id = pp.id AND bl.type = 'BASELINE' AND bl.cycle = LEAST(:cycle, pp.current_cycle)
            LEFT JOIN assessment_response md ON md.participant_id = pp.id AND md.type = 'MID' AND md.cycle = LEAST(:cycle, pp.current_cycle)
            JOIN assessment_response fn ON fn.participant_id = pp.id AND fn.type = 'FINAL' AND fn.cycle = LEAST(:cycle, pp.current_cycle)
            LEFT JOIN user_allocation ua ON ua.package_id = pr.coaching_package_id AND ua.username = pp.username
            WHERE pp.program_id = :programId AND pp.username = :username
            """)
    Optional<JourneyProjection> findJourneyData(Long programId, String username, int cycle);

    @Query("""
            SELECT
                p.id                            AS program_id,
                p.name                          AS program_name,
                pp.username                     AS username,
                u.first_name                    AS first_name,
                u.last_name                     AS last_name,
                pp.status                       AS enrollment_status,
                COALESCE(ua.consumed_units, 0)  AS attendance_count,
                CASE
                    WHEN wp_stats.total = 0 THEN 0
                    ELSE wp_stats.responded::float / wp_stats.total
                END                             AS practice_completion_rate
            FROM program_participant pp
            JOIN program p ON p.id = pp.program_id
            JOIN coaching_package cp ON cp.id = p.coaching_package_id
            JOIN users u ON u.username = pp.username
            LEFT JOIN user_allocation ua ON ua.username = pp.username AND ua.package_id = cp.id
            LEFT JOIN LATERAL (
                SELECT
                    COUNT(*)                                                 AS total,
                    COUNT(*) FILTER (WHERE friday_response IS NOT NULL)      AS responded
                FROM weekly_practice
                WHERE participant_id = pp.id
            ) wp_stats ON true
            WHERE pp.manager_username = :managerUsername
              AND cp.company_id = :companyId
            ORDER BY p.name, u.last_name, u.first_name
            """)
    List<ManagerParticipantRow> findManagedParticipants(String managerUsername, Long companyId);
}
