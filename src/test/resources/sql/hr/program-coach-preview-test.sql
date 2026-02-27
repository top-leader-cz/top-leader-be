-- Seed expertise categories and focus area mappings (normally done by Flyway V1.0.0.17)
INSERT INTO expertise_category (key, name, coach_fields) VALUES
('ec.leadership', 'Leadership & Management', '["leadership","executive","delegation","strategic_thinking","teams","management"]'),
('ec.communication', 'Communication & Influence', '["communication","feedback_culture","conflict"]'),
('ec.resilience', 'Resilience & Wellbeing', '["resilience","stress_management","wellbeing"]');

INSERT INTO focus_area (key) VALUES
('fa.giving-feedback'), ('fa.delegation'), ('fa.communication');

INSERT INTO focus_area_category_mapping (focus_area_key, category_key) VALUES
('fa.giving-feedback', 'ec.communication'),
('fa.delegation', 'ec.leadership'),
('fa.communication', 'ec.communication');

-- Company and HR user
INSERT INTO company (id, name) VALUES (100, 'Test Corp');

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES ('hr_prog', 'pass', '["HR","USER"]', 'AUTHORIZED', 'UTC', 'Hr', 'Manager', 100);

-- Coaches with fields and languages for matching
INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name)
VALUES
    ('coach_en_lead', 'pass', '["USER","COACH"]', 'AUTHORIZED', 'UTC', 'Alice', 'Leader'),
    ('coach_en_comm', 'pass', '["USER","COACH"]', 'AUTHORIZED', 'UTC', 'Bob', 'Communicator'),
    ('coach_cs_lead', 'pass', '["USER","COACH"]', 'AUTHORIZED', 'UTC', 'Karel', 'Vedouci'),
    ('coach_en_other', 'pass', '["USER","COACH"]', 'AUTHORIZED', 'UTC', 'Charlie', 'Other'),
    ('coach_de_lead', 'pass', '["USER","COACH"]', 'AUTHORIZED', 'UTC', 'Hans', 'Fuhrer'),
    ('coach_private', 'pass', '["USER","COACH"]', 'AUTHORIZED', 'UTC', 'Private', 'Coach');

INSERT INTO coach (username, public_profile, bio, experience_since, rate, rate_order, free_slots, priority, languages, fields) VALUES
('coach_en_lead', true, 'Leadership expert', '2015-01-01', '$', 1, true, 10, '["en"]', '["leadership","executive","teams"]'),
('coach_en_comm', true, 'Communication specialist', '2016-01-01', '$$', 2, true, 8, '["en"]', '["communication","feedback_culture","conflict"]'),
('coach_cs_lead', true, 'Czech leadership coach', '2018-01-01', '$', 1, true, 7, '["cs"]', '["leadership","management","delegation"]'),
('coach_en_other', true, 'Career coach', '2017-01-01', '$$', 2, true, 5, '["en"]', '["career","confidence","change"]'),
('coach_de_lead', true, 'German leadership', '2014-01-01', '$$$', 3, true, 9, '["de"]', '["leadership","strategic_thinking"]'),
('coach_private', false, 'Private coach', '2020-01-01', '$', 1, false, 1, '["en"]', '["leadership"]');
