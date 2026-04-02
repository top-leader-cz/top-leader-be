-- =============================================================================
-- Local Development Seed Data
-- =============================================================================
-- All passwords: parole1
-- Safe to re-run (uses ON CONFLICT DO NOTHING)
--
-- Usage: make db-seed
-- =============================================================================

-- BCrypt hash of 'parole1' (cost factor 10)
-- $2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq

-- =====================
-- Coach Rates
-- =====================
INSERT INTO coach_rate (rate_name, rate_credit, rate_order)
VALUES ('$', 110, 1),
       ('$$', 165, 2),
       ('$$$', 275, 3)
ON CONFLICT DO NOTHING;

-- =====================
-- Companies
-- =====================
INSERT INTO company (id, name, business_strategy)
VALUES (1, 'Acme Corp', 'Leading innovation in tech solutions'),
       (2, 'Beta Inc', 'Global consulting and services')
ON CONFLICT DO NOTHING;

SELECT setval('company_id_seq', GREATEST(nextval('company_id_seq'), 3), false);

-- =====================
-- Users
-- =====================
INSERT INTO users (username, email, password, first_name, last_name, authorities, status, time_zone, company_id, locale, credit, scheduled_credit, paid_credit, requested_credit, sum_requested_credit)
VALUES
    -- ADMIN
    ('admin@local.dev', 'admin@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Admin', 'User', '["USER", "ADMIN"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),

    -- Regular users
    ('user1@local.dev', 'user1@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Alice', 'Johnson', '["USER"]', 'AUTHORIZED', 'UTC', 1, 'en', 10, 0, 0, 0, 0),

    ('user2@local.dev', 'user2@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Bob', 'Smith', '["USER"]', 'AUTHORIZED', 'UTC', 2, 'en', 10, 0, 0, 0, 0),

    -- Coaches
    ('coach1@local.dev', 'coach1@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Carol', 'Coach', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', 1, 'en', 0, 0, 0, 0, 0),

    ('coach2@local.dev', 'coach2@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'David', 'Mentor', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', 2, 'en', 0, 0, 0, 0, 0),

    -- HR
    ('hr1@local.dev', 'hr1@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Helen', 'Resources', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 1, 'en', 0, 0, 0, 0, 0),

    -- Manager
    ('manager1@local.dev', 'manager1@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Mike', 'Manager', '["USER", "MANAGER"]', 'AUTHORIZED', 'UTC', 1, 'en', 0, 0, 0, 0, 0),

    -- Multi-role user
    ('multi@local.dev', 'multi@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Max', 'Multi', '["USER", "COACH", "MANAGER"]', 'AUTHORIZED', 'UTC', 1, 'en', 10, 0, 0, 0, 0),

    -- E2E HR user (dedicated, no manual data conflicts)
    ('hr-e2e@local.dev', 'hr-e2e@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'E2E', 'HrTest', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 1, 'en', 0, 0, 0, 0, 0)
ON CONFLICT (username) DO NOTHING;

-- Assign coaches to users
UPDATE users SET coach = 'coach1@local.dev' WHERE username = 'user1@local.dev' AND coach IS NULL;
UPDATE users SET coach = 'coach2@local.dev' WHERE username = 'user2@local.dev' AND coach IS NULL;

-- =====================
-- Coach profiles
-- =====================
INSERT INTO coach (username, bio, experience_since, public_profile, rate, rate_order, internal_rate, free_slots, priority, primary_roles, fields, languages)
VALUES
    ('coach1@local.dev', 'Experienced leadership coach specializing in executive development.',
     '2018-01-01', true, '$$', 2, 165, true, 1,
     '["COACH"]', '["Leadership", "Executive"]', '["en", "cs"]'),

    ('coach2@local.dev', 'Career and performance coach with 10+ years experience.',
     '2015-06-01', true, '$$$', 3, 275, true, 2,
     '["COACH"]', '["Career", "Performance"]', '["en", "de"]'),

    ('multi@local.dev', 'Coach and manager with hands-on experience.',
     '2020-03-01', true, '$', 1, 110, true, 0,
     '["COACH", "MENTOR"]', '["Team Building"]', '["en"]')
ON CONFLICT (username) DO NOTHING;

-- =====================
-- Manager relationships
-- =====================
INSERT INTO users_managers (manager_username, user_username)
VALUES ('manager1@local.dev', 'user1@local.dev'),
       ('multi@local.dev', 'user2@local.dev')
ON CONFLICT DO NOTHING;

-- =====================
-- User coach rate access
-- =====================
INSERT INTO user_coach_rates (username, rate_name)
VALUES ('user1@local.dev', '$'),
       ('user1@local.dev', '$$'),
       ('user2@local.dev', '$'),
       ('user2@local.dev', '$$'),
       ('user2@local.dev', '$$$'),
       ('multi@local.dev', '$')
ON CONFLICT DO NOTHING;

-- =====================
-- Company coach rate access
-- =====================
INSERT INTO company_coach_rates (company_id, rate_name)
VALUES (1, '$'),
       (1, '$$'),
       (2, '$'),
       (2, '$$'),
       (2, '$$$')
ON CONFLICT DO NOTHING;

-- =====================
-- Feedback questions (from misc/questions.sql)
-- =====================
INSERT INTO fb_question (key) VALUES
    ('question.general.work-in-respectful-manners'),
    ('question.consider-other-team-members'),
    ('question.general.effectively-solve-problems'),
    ('question.general.responsive-to-their-team'),
    ('question.general.work-under-pressure-to-meet-deadlines'),
    ('question.general.clear-vision-that-aligns-with-the-organisation'),
    ('question.leadership.provide-solutions'),
    ('question.leadership.demonstrating-leadership'),
    ('question.leadership.accountability-for-the-work'),
    ('question.leadership.others-look-to-help'),
    ('question.leadership.duties-without-issues'),
    ('question.leadership.bring-ideas'),
    ('question.leadership.supervise-work'),
    ('question.communication.listen-well'),
    ('question.communication.communicate-effectively'),
    ('question.communication.ask-for-more-information'),
    ('question.communication.communicate-well-in-writing'),
    ('question.communication.speak-clearly'),
    ('question.communication.ideas-to-others'),
    ('question.communication.opportunity-for-discussion'),
    ('question.interpersonal.work-well-with-others'),
    ('question.interpersonal.show-respect'),
    ('question.interpersonal.manage-the-emotions'),
    ('question.interpersonal.manage-the-stress'),
    ('question.interpersonal.have-conflict-with-others'),
    ('question.interpersonal.exhibit-the-core-values'),
    ('question.interpersonal.collaborate-with-others'),
    ('question.interpersonal.other-staff-will-turn-to'),
    ('1uestion.problemsolving.effective-at-evaluating'),
    ('question.problemsolving.suggest-useful-solutions'),
    ('question.problemsolving.recognize-problem'),
    ('question.problemsolving.communicate-the-problem-to-others'),
    ('question.problemsolving.work-independently'),
    ('question.problemsolving.feel-confident-in-exploring-problem'),
    ('question.problemsolving.provide-creative-solution'),
    ('question.problemsolving.understand-the-impacts-and-dependencies'),
    ('question.organizational.know-goals'),
    ('question.organizational.know-strategic-vision'),
    ('question.organizational.live-values'),
    ('question.organizational.active-in-meetings'),
    ('question.organizational.recmmend-the-company'),
    ('question.organizational.showing-engagement'),
    ('question.organizational.provide-feedback-about-ideas'),
    ('question.motivation.appear-motivated-by-their-job'),
    ('question.motivation.communicate-motivated'),
    ('question.motivation.difficullt-to-motivate'),
    ('question.motivation.level-of-motivation'),
    ('question.motivation.share-their-work'),
    ('question.motivation.motivate-others'),
    ('question.efficiency.complete-their-tasks'),
    ('question.efficiency.sense-of-collaboration'),
    ('question.efficiency.daily-work-effectively'),
    ('question.efficiency.work-finished-right'),
    ('question.efficiency.work-on-time'),
    ('question.efficiency.maintain-high-standards'),
    ('question.efficiency.exceed-expectations'),
    ('question.efficiency.improve-processes')
ON CONFLICT DO NOTHING;

-- =====================
-- Coaching packages & allocations
-- =====================
INSERT INTO coaching_package (id, company_id, pool_type, total_units, valid_from, valid_to, status, created_at, created_by)
VALUES
    (nextval('coaching_package_id_seq'), 1, 'CORE', 100, '2025-01-01', '2025-12-31', 'ACTIVE', now(), 'hr1@local.dev'),
    (nextval('coaching_package_id_seq'), 2, 'MASTER', 50, '2025-01-01', '2025-12-31', 'ACTIVE', now(), 'admin@local.dev')
ON CONFLICT DO NOTHING;

-- Allocate credits to users from company 1 package
INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_at, created_by)
SELECT nextval('user_allocation_id_seq'), 1, cp.id, 'user1@local.dev', 10, 0, 'ACTIVE', now(), 'hr1@local.dev'
FROM coaching_package cp WHERE cp.company_id = 1 AND cp.pool_type = 'CORE'
ON CONFLICT DO NOTHING;

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_at, created_by)
SELECT nextval('user_allocation_id_seq'), 2, cp.id, 'user2@local.dev', 5, 0, 'ACTIVE', now(), 'admin@local.dev'
FROM coaching_package cp WHERE cp.company_id = 2 AND cp.pool_type = 'MASTER'
ON CONFLICT DO NOTHING;

-- =====================
-- Assessment questions (5 per focus area, Likert scale)
-- =====================
INSERT INTO assessment_question (focus_area_key, question_order, question_text)
SELECT fa.key, q.ord, q.txt
FROM focus_area fa
CROSS JOIN (VALUES
    (1, 'I feel confident in my ability in this area'),
    (2, 'I regularly apply skills related to this area'),
    (3, 'I seek feedback from others on this topic'),
    (4, 'I can identify specific areas for improvement'),
    (5, 'I have a clear plan to develop further in this area')
) AS q(ord, txt)
ON CONFLICT DO NOTHING;

-- =====================
-- Done
-- =====================
-- Login with any user above using password: parole1
-- Example: admin@local.dev / parole1
