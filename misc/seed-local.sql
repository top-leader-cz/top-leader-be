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
INSERT INTO company (id, name, business_strategy, programs_enabled)
VALUES (1, 'company1', 'Tech company with coaching programs enabled', true),
       (2, 'company2', 'Consulting company without programs', false)
ON CONFLICT DO NOTHING;

SELECT setval('company_id_seq', GREATEST(nextval('company_id_seq'), 3), false);

-- =====================
-- Users
-- =====================
INSERT INTO users (username, email, password, first_name, last_name, authorities, status, time_zone, company_id, locale, credit, scheduled_credit, paid_credit, requested_credit, sum_requested_credit)
VALUES
    -- ADMIN (no company)
    ('admin@local.dev', 'admin@local.dev',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Admin', 'User', '["USER", "ADMIN"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),

    -- ===== company1 (programs enabled) =====
    -- HR
    ('hr1@company1.local', 'hr1@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Hannah', 'Resources', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 1, 'en', 0, 0, 0, 0, 0),
    ('hr2@company1.local', 'hr2@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Hector', 'Recruiting', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 1, 'en', 0, 0, 0, 0, 0),
    -- Managers
    ('mgr1@company1.local', 'mgr1@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Mike', 'Manager', '["USER", "MANAGER"]', 'AUTHORIZED', 'UTC', 1, 'en', 0, 0, 0, 0, 0),
    ('mgr2@company1.local', 'mgr2@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Maria', 'Lead', '["USER", "MANAGER"]', 'AUTHORIZED', 'UTC', 1, 'en', 0, 0, 0, 0, 0),
    -- Regular users
    ('user1@company1.local', 'user1@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Alice', 'Adams', '["USER"]', 'AUTHORIZED', 'UTC', 1, 'en', 10, 0, 0, 0, 0),
    ('user2@company1.local', 'user2@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Bob', 'Brown', '["USER"]', 'AUTHORIZED', 'UTC', 1, 'en', 10, 0, 0, 0, 0),
    ('user3@company1.local', 'user3@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Charlie', 'Clark', '["USER"]', 'AUTHORIZED', 'UTC', 1, 'en', 10, 0, 0, 0, 0),
    ('user4@company1.local', 'user4@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Diana', 'Davis', '["USER"]', 'AUTHORIZED', 'UTC', 1, 'en', 10, 0, 0, 0, 0),
    ('user5@company1.local', 'user5@company1.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Edward', 'Evans', '["USER"]', 'AUTHORIZED', 'UTC', 1, 'en', 10, 0, 0, 0, 0),

    -- ===== company2 (programs disabled) =====
    -- HR
    ('hr1@company2.local', 'hr1@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Helen', 'Hart', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 2, 'en', 0, 0, 0, 0, 0),
    ('hr2@company2.local', 'hr2@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Hugo', 'Hill', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 2, 'en', 0, 0, 0, 0, 0),
    -- Managers
    ('mgr1@company2.local', 'mgr1@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Marcus', 'Moore', '["USER", "MANAGER"]', 'AUTHORIZED', 'UTC', 2, 'en', 0, 0, 0, 0, 0),
    ('mgr2@company2.local', 'mgr2@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Megan', 'Miller', '["USER", "MANAGER"]', 'AUTHORIZED', 'UTC', 2, 'en', 0, 0, 0, 0, 0),
    -- Regular users
    ('user1@company2.local', 'user1@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Frank', 'Fisher', '["USER"]', 'AUTHORIZED', 'UTC', 2, 'en', 10, 0, 0, 0, 0),
    ('user2@company2.local', 'user2@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Grace', 'Green', '["USER"]', 'AUTHORIZED', 'UTC', 2, 'en', 10, 0, 0, 0, 0),
    ('user3@company2.local', 'user3@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Henry', 'Hughes', '["USER"]', 'AUTHORIZED', 'UTC', 2, 'en', 10, 0, 0, 0, 0),
    ('user4@company2.local', 'user4@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Irene', 'Irving', '["USER"]', 'AUTHORIZED', 'UTC', 2, 'en', 10, 0, 0, 0, 0),
    ('user5@company2.local', 'user5@company2.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Jack', 'Jenkins', '["USER"]', 'AUTHORIZED', 'UTC', 2, 'en', 10, 0, 0, 0, 0),

    -- ===== Coaches (no company) =====
    ('coach1@no-company.local', 'coach1@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Carol', 'Coach', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach2@no-company.local', 'coach2@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'David', 'Dawson', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach3@no-company.local', 'coach3@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Eva', 'Ellis', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach4@no-company.local', 'coach4@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Felix', 'Fox', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach5@no-company.local', 'coach5@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Gina', 'Grant', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach6@no-company.local', 'coach6@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Ivan', 'Ivanov', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach7@no-company.local', 'coach7@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Julia', 'James', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach8@no-company.local', 'coach8@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Karl', 'King', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach9@no-company.local', 'coach9@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Laura', 'Lane', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0),
    ('coach10@no-company.local', 'coach10@no-company.local',
     '$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq',
     'Niko', 'Nash', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', NULL, 'en', 0, 0, 0, 0, 0)
ON CONFLICT (username) DO NOTHING;

-- =====================
-- Coach profiles
-- =====================
INSERT INTO coach (username, bio, experience_since, public_profile, rate, rate_order, internal_rate, free_slots, priority, primary_roles, fields, languages)
VALUES
    ('coach1@no-company.local', 'Leadership coach specializing in executive development.',
     '2016-01-01', true, '$', 1, 110, true, 1,
     '["COACH"]', '["leadership", "executive"]', '["en", "cs"]'),
    ('coach2@no-company.local', 'Career and performance coach with 10+ years experience.',
     '2014-06-01', true, '$$', 2, 165, true, 2,
     '["COACH"]', '["career", "performance"]', '["en", "de"]'),
    ('coach3@no-company.local', 'Communication and conflict resolution specialist.',
     '2018-03-01', true, '$', 1, 110, true, 3,
     '["COACH"]', '["communication", "conflict"]', '["en", "fr"]'),
    ('coach4@no-company.local', 'Team building and organizational development coach.',
     '2015-09-01', true, '$$$', 3, 275, true, 4,
     '["COACH"]', '["teams", "organizational_development"]', '["en"]'),
    ('coach5@no-company.local', 'Resilience and wellbeing coach for high-pressure environments.',
     '2019-01-01', true, '$$', 2, 165, true, 5,
     '["COACH"]', '["resilience", "stress_management", "wellbeing"]', '["en", "es"]'),
    ('coach6@no-company.local', 'Innovation and transformation coach for tech leaders.',
     '2017-04-01', true, '$$$', 3, 275, true, 6,
     '["COACH"]', '["innovation_and_creativity", "transformations"]', '["en", "ru"]'),
    ('coach7@no-company.local', 'Self-leadership and confidence building specialist.',
     '2020-01-01', true, '$', 1, 110, true, 7,
     '["COACH"]', '["self_leadership", "confidence", "emotional_intelligence"]', '["en"]'),
    ('coach8@no-company.local', 'Sales performance and business strategy coach.',
     '2013-11-01', true, '$$$', 3, 275, true, 8,
     '["COACH"]', '["performance", "sales", "business"]', '["en", "de"]'),
    ('coach9@no-company.local', 'Mentor and facilitator focused on diversity and inclusion.',
     '2018-07-01', true, '$$', 2, 165, true, 9,
     '["COACH", "MENTOR"]', '["diversity", "mentorship", "facilitation"]', '["en", "pt"]'),
    ('coach10@no-company.local', 'Remote leadership and cross-cultural management expert.',
     '2016-05-01', true, '$$', 2, 165, true, 10,
     '["COACH"]', '["remote_leadership", "cross_cultural_leadership", "management"]', '["en", "ja"]')
ON CONFLICT (username) DO NOTHING;

-- =====================
-- Manager relationships
-- =====================
-- company1: mgr1 manages user1-3, mgr2 manages user4-5
INSERT INTO users_managers (manager_username, user_username)
VALUES ('mgr1@company1.local', 'user1@company1.local'),
       ('mgr1@company1.local', 'user2@company1.local'),
       ('mgr1@company1.local', 'user3@company1.local'),
       ('mgr2@company1.local', 'user4@company1.local'),
       ('mgr2@company1.local', 'user5@company1.local')
ON CONFLICT DO NOTHING;

-- company2: mgr1 manages user1-3, mgr2 manages user4-5
INSERT INTO users_managers (manager_username, user_username)
VALUES ('mgr1@company2.local', 'user1@company2.local'),
       ('mgr1@company2.local', 'user2@company2.local'),
       ('mgr1@company2.local', 'user3@company2.local'),
       ('mgr2@company2.local', 'user4@company2.local'),
       ('mgr2@company2.local', 'user5@company2.local')
ON CONFLICT DO NOTHING;

-- Assign coaches to some users
UPDATE users SET coach = 'coach1@no-company.local' WHERE username = 'user1@company1.local' AND coach IS NULL;
UPDATE users SET coach = 'coach2@no-company.local' WHERE username = 'user2@company1.local' AND coach IS NULL;
UPDATE users SET coach = 'coach3@no-company.local' WHERE username = 'user1@company2.local' AND coach IS NULL;
UPDATE users SET coach = 'coach4@no-company.local' WHERE username = 'user2@company2.local' AND coach IS NULL;

-- =====================
-- Company coach rate access
-- =====================
INSERT INTO company_coach_rates (company_id, rate_name)
VALUES (1, '$'),
       (1, '$$'),
       (1, '$$$'),
       (2, '$'),
       (2, '$$')
ON CONFLICT DO NOTHING;

-- =====================
-- User coach rate access (for users with assigned coaches)
-- =====================
INSERT INTO user_coach_rates (username, rate_name)
VALUES ('user1@company1.local', '$'),
       ('user1@company1.local', '$$'),
       ('user2@company1.local', '$'),
       ('user2@company1.local', '$$'),
       ('user2@company1.local', '$$$'),
       ('user1@company2.local', '$'),
       ('user1@company2.local', '$$'),
       ('user2@company2.local', '$'),
       ('user2@company2.local', '$$')
ON CONFLICT DO NOTHING;

-- =====================
-- Feedback questions
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
    (nextval('coaching_package_id_seq'), 1, 'CORE', 200, '2026-01-01', '2026-12-31', 'ACTIVE', now(), 'hr1@company1.local'),
    (nextval('coaching_package_id_seq'), 2, 'CORE', 100, '2026-01-01', '2026-12-31', 'ACTIVE', now(), 'hr1@company2.local')
ON CONFLICT DO NOTHING;

-- Allocate credits to users with assigned coaches
INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_at, created_by)
SELECT nextval('user_allocation_id_seq'), 1, cp.id, 'user1@company1.local', 10, 0, 'ACTIVE', now(), 'hr1@company1.local'
FROM coaching_package cp WHERE cp.company_id = 1 AND cp.pool_type = 'CORE'
ON CONFLICT DO NOTHING;

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_at, created_by)
SELECT nextval('user_allocation_id_seq'), 1, cp.id, 'user2@company1.local', 10, 0, 'ACTIVE', now(), 'hr1@company1.local'
FROM coaching_package cp WHERE cp.company_id = 1 AND cp.pool_type = 'CORE'
ON CONFLICT DO NOTHING;

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_at, created_by)
SELECT nextval('user_allocation_id_seq'), 2, cp.id, 'user1@company2.local', 10, 0, 'ACTIVE', now(), 'hr1@company2.local'
FROM coaching_package cp WHERE cp.company_id = 2 AND cp.pool_type = 'CORE'
ON CONFLICT DO NOTHING;

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_at, created_by)
SELECT nextval('user_allocation_id_seq'), 2, cp.id, 'user2@company2.local', 10, 0, 'ACTIVE', now(), 'hr1@company2.local'
FROM coaching_package cp WHERE cp.company_id = 2 AND cp.pool_type = 'CORE'
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
