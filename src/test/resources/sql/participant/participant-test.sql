INSERT INTO company (id, name) VALUES (100, 'Test Corp');

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES ('participant1', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Jan', 'Novak', 100),
       ('participant2', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Eva', 'Svoboda', 100);

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (100, 100, 'CORE', 20, 'ACTIVE', '2026-03-15 00:00:00', '2026-06-13 00:00:00', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, goal, status, duration_days, sessions_per_participant,
                     coach_assignment_model, focus_areas, created_at, created_by)
VALUES (1, 100, 'Leadership 90d', 'Improve feedback culture', 'CREATED', 90, 5,
        'PARTICIPANT_CHOOSES', '["fa.giving-feedback", "fa.delegation"]', NOW(), 'hr_prog');

INSERT INTO focus_area (key) VALUES
    ('fa.giving-feedback'),
    ('fa.delegation'),
    ('fa.self-awareness'),
    ('fa.strategic-thinking')
ON CONFLICT DO NOTHING;

INSERT INTO program_participant (id, program_id, username, status, current_cycle, created_by, created_at)
VALUES (1, 1, 'participant1', 'INVITED', 1, 'hr_prog', NOW());

INSERT INTO program_participant (id, program_id, username, status, current_cycle, focus_area, personal_goal, enrolled_at, created_by, created_at)
VALUES (2, 1, 'participant2', 'ACTIVE', 1, 'fa.giving-feedback', 'Be a better leader', NOW() - INTERVAL '60 days', 'hr_prog', NOW() - INTERVAL '60 days');

INSERT INTO assessment_question (id, focus_area_key, question_order, question_text)
VALUES
    (1, 'fa.giving-feedback', 1, 'I feel confident giving constructive feedback.'),
    (2, 'fa.giving-feedback', 2, 'I give feedback regularly to my team.'),
    (3, 'fa.giving-feedback', 3, 'I receive feedback well from others.'),
    (4, 'fa.giving-feedback', 4, 'I adjust my feedback style based on the person.'),
    (5, 'fa.giving-feedback', 5, 'Feedback in my team leads to visible improvements.');

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES ('participant3', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Petr', 'Motloch', 100),
       ('participant4', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Marie', 'Kralova', 100);

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (101, 100, 'CORE', 20, 'ACTIVE', NOW(), NOW() + INTERVAL '180 days', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, goal, status, duration_days, cycle_length_days, sessions_per_participant,
                     coach_assignment_model, focus_areas, created_at, created_by)
VALUES (2, 101, 'Leadership 180d', 'Build leadership skills', 'CREATED', 180, 90, 5,
        'PARTICIPANT_CHOOSES', '["fa.giving-feedback"]', NOW(), 'hr_prog');

INSERT INTO program_participant (id, program_id, username, status, current_cycle, focus_area, personal_goal, enrolled_at, created_by, created_at)
VALUES (3, 1, 'participant3', 'ACTIVE', 1, 'fa.giving-feedback', 'Lead by example', NOW() - INTERVAL '95 days', 'hr_prog', NOW() - INTERVAL '95 days');

INSERT INTO program_participant (id, program_id, username, status, current_cycle, focus_area, personal_goal, enrolled_at, created_by, created_at)
VALUES (4, 2, 'participant4', 'ACTIVE', 1, 'fa.giving-feedback', 'Grow as leader', NOW() - INTERVAL '95 days', 'hr_prog', NOW() - INTERVAL '95 days');

INSERT INTO assessment_response (id, participant_id, type, cycle, focus_area_key, q1, q2, q3, q4, q5, completed_at)
VALUES (1, 2, 'BASELINE', 1, 'fa.giving-feedback', 2, 3, 2, 3, 4, NOW() - INTERVAL '58 days');

INSERT INTO assessment_response (id, participant_id, type, cycle, focus_area_key, q1, q2, q3, q4, q5, completed_at)
VALUES (2, 3, 'BASELINE', 1, 'fa.giving-feedback', 2, 3, 2, 3, 4, NOW() - INTERVAL '93 days');

INSERT INTO assessment_response (id, participant_id, type, cycle, focus_area_key, q1, q2, q3, q4, q5, completed_at)
VALUES (3, 3, 'MID', 1, 'fa.giving-feedback', 3, 4, 3, 4, 5, NOW() - INTERVAL '48 days');

INSERT INTO assessment_response (id, participant_id, type, cycle, focus_area_key, q1, q2, q3, q4, q5, completed_at)
VALUES (4, 4, 'BASELINE', 1, 'fa.giving-feedback', 2, 3, 2, 3, 4, NOW() - INTERVAL '93 days');

INSERT INTO assessment_response (id, participant_id, type, cycle, focus_area_key, q1, q2, q3, q4, q5, completed_at)
VALUES (5, 4, 'MID', 1, 'fa.giving-feedback', 3, 4, 3, 4, 5, NOW() - INTERVAL '48 days');

ALTER SEQUENCE assessment_response_id_seq RESTART WITH 100;

INSERT INTO weekly_practice (id, participant_id, cycle, week_number, text, source, created_at)
VALUES (1, 2, 1, 1, 'Give feedback to one team member today', 'AI', NOW() - INTERVAL '60 days');

INSERT INTO weekly_practice (id, participant_id, cycle, week_number, text, source, friday_response, created_at)
VALUES (2, 3, 1, 1, 'Practice direct feedback', 'AI', 'YES', NOW() - INTERVAL '90 days'),
       (3, 3, 1, 2, 'Give constructive feedback in standup', 'AI', 'PARTIAL', NOW() - INTERVAL '83 days'),
       (4, 3, 1, 3, 'Schedule feedback 1:1', 'EDITED', NULL, NOW() - INTERVAL '76 days'),
       (5, 4, 1, 1, 'Practice active listening', 'AI', 'YES', NOW() - INTERVAL '90 days');

ALTER SEQUENCE weekly_practice_id_seq RESTART WITH 100;

INSERT INTO ai_prompt (id, value) VALUES ('WEEKLY_PRACTICE',
'You are an expert leadership coach. Generate exactly 5 distinct weekly practice suggestions for a participant in a corporate coaching program.

Focus area: {focusArea}
Personal goal: {personalGoal}
Program goal: {programGoal}

IMPORTANT: Respond in {language}.
Return ONLY a valid JSON array of 5 strings.');

INSERT INTO ai_prompt (id, value) VALUES ('GOAL_SUGGESTIONS',
'Generate 3 personal goal suggestions for focus area {focusArea} and program goal {programGoal}.
IMPORTANT: Respond in {language}.
Return ONLY a valid JSON array of 3 strings.');

-- Program with full area library enabled (for S2-003 grouping tests)
INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES ('participant5', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Lucie', 'Library', 100);

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (102, 100, 'CORE', 10, 'ACTIVE', NOW(), NOW() + INTERVAL '90 days', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, goal, status, duration_days, sessions_per_participant,
                     coach_assignment_model, focus_areas, allow_full_area_library, created_at, created_by)
VALUES (3, 102, 'Open library program', 'Any growth', 'CREATED', 90, 5,
        'PARTICIPANT_CHOOSES', '["fa.giving-feedback"]', TRUE, NOW(), 'hr_prog');

INSERT INTO program_participant (id, program_id, username, status, current_cycle, created_by, created_at)
VALUES (5, 3, 'participant5', 'INVITED', 1, 'hr_prog', NOW());
