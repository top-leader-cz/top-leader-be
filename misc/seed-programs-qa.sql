-- =============================================================================
-- QA Seed: 2 programs per company + all company users as participants
-- =============================================================================
-- Run via psql against QA DB (Cloud SQL proxy on localhost:5432):
--   psql -h localhost -U postgres -d topleader -f misc/seed-programs-qa.sql
-- =============================================================================

DO $$
DECLARE
    v_company         RECORD;
    v_user            RECORD;
    v_pkg_id_1        BIGINT;
    v_pkg_id_2        BIGINT;
    v_program_id_1    BIGINT;
    v_program_id_2    BIGINT;
BEGIN
    FOR v_company IN
        SELECT id, name FROM company ORDER BY id
    LOOP
        RAISE NOTICE 'Processing company: % (id=%)', v_company.name, v_company.id;

        -- ── Coaching package 1: 90-day program ─────────────────────────────
        INSERT INTO coaching_package (company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
        VALUES (v_company.id, 'CORE', 40, 'ACTIVE',
                NOW(),
                NOW() + INTERVAL '90 days',
                'admin', NOW())
        RETURNING id INTO v_pkg_id_1;

        -- ── Coaching package 2: 180-day program ────────────────────────────
        INSERT INTO coaching_package (company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
        VALUES (v_company.id, 'CORE', 40, 'ACTIVE',
                NOW(),
                NOW() + INTERVAL '180 days',
                'admin', NOW())
        RETURNING id INTO v_pkg_id_2;

        -- ── Program 1 ──────────────────────────────────────────────────────
        INSERT INTO program (coaching_package_id, name, milestone_date, created_at, created_by)
        VALUES (v_pkg_id_1,
                v_company.name || ' – Leadership Program',
                NOW() + INTERVAL '90 days',
                NOW(), 'admin')
        RETURNING id INTO v_program_id_1;

        -- ── Program 2 ──────────────────────────────────────────────────────
        INSERT INTO program (coaching_package_id, name, milestone_date, created_at, created_by)
        VALUES (v_pkg_id_2,
                v_company.name || ' – Development Program',
                NOW() + INTERVAL '180 days',
                NOW(), 'admin')
        RETURNING id INTO v_program_id_2;

        RAISE NOTICE '  Created programs: % and %', v_program_id_1, v_program_id_2;

        -- ── Participants: all USER-role users in this company ──────────────
        FOR v_user IN
            SELECT username, coach
            FROM users
            WHERE company_id = v_company.id
              AND authorities::text LIKE '%USER%'
        LOOP
            -- Allocate user to package 1
            INSERT INTO user_allocation (company_id, package_id, username, allocated_units, consumed_units, status, created_by, created_at)
            VALUES (v_company.id, v_pkg_id_1, v_user.username, 4, 0, 'ACTIVE', 'admin', NOW())
            ON CONFLICT (package_id, username) DO NOTHING;

            -- Allocate user to package 2
            INSERT INTO user_allocation (company_id, package_id, username, allocated_units, consumed_units, status, created_by, created_at)
            VALUES (v_company.id, v_pkg_id_2, v_user.username, 4, 0, 'ACTIVE', 'admin', NOW())
            ON CONFLICT (package_id, username) DO NOTHING;

            -- Add to program_participant (program 1)
            INSERT INTO program_participant (program_id, username, coach_username, status, created_by, created_at)
            VALUES (v_program_id_1, v_user.username, v_user.coach, 'ON_TRACK', 'admin', NOW())
            ON CONFLICT (program_id, username) DO NOTHING;

            -- Add to program_participant (program 2)
            INSERT INTO program_participant (program_id, username, coach_username, status, created_by, created_at)
            VALUES (v_program_id_2, v_user.username, v_user.coach, 'ON_TRACK', 'admin', NOW())
            ON CONFLICT (program_id, username) DO NOTHING;
        END LOOP;

        RAISE NOTICE '  Participants added for company %', v_company.name;
    END LOOP;

    RAISE NOTICE 'Done.';
END $$;
