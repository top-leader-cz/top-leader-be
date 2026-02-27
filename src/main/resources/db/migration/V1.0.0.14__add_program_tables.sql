-- Add last login tracking
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

-- Index on coaching_package.company_id (used by program queries)
CREATE INDEX IF NOT EXISTS idx_coaching_package_company_id ON coaching_package (company_id);

-- Program sequences
CREATE SEQUENCE IF NOT EXISTS program_id_seq;
CREATE SEQUENCE IF NOT EXISTS program_participant_id_seq;

-- Program: 1-1 superset of coaching_package
CREATE TABLE IF NOT EXISTS program
(
    id                  BIGINT       DEFAULT nextval('program_id_seq') NOT NULL PRIMARY KEY,
    coaching_package_id BIGINT       NOT NULL UNIQUE REFERENCES coaching_package (id),
    name                VARCHAR(255) NOT NULL,
    goal                TEXT,
    target_group        VARCHAR(255),
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    duration_days       INT,
    focus_areas              JSONB        DEFAULT '[]'::jsonb NOT NULL,
    sessions_per_participant INT,
    recommended_cadence      VARCHAR(255),
    coach_assignment_model   VARCHAR(30)  NOT NULL DEFAULT 'PARTICIPANT_CHOOSES',
    shortlisted_coaches      JSONB        DEFAULT '[]'::jsonb NOT NULL,
    micro_actions_enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    enabled_options          JSONB        DEFAULT '[]'::jsonb NOT NULL,
    milestone_date           TIMESTAMP,
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP         NOT NULL,
    created_by          VARCHAR(255) NOT NULL,
    updated_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_program_coaching_package_id ON program (coaching_package_id);

-- Per-participant link within a program
CREATE TABLE IF NOT EXISTS program_participant
(
    id             BIGINT       DEFAULT nextval('program_participant_id_seq') NOT NULL PRIMARY KEY,
    program_id     BIGINT       NOT NULL REFERENCES program (id),
    username       VARCHAR(255) NOT NULL,
    coach_username VARCHAR(255),
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP                     NOT NULL,
    created_by     VARCHAR(255) NOT NULL,
    CONSTRAINT uk_program_participant UNIQUE (program_id, username)
);

CREATE INDEX IF NOT EXISTS idx_program_participant_program_id ON program_participant (program_id);
CREATE INDEX IF NOT EXISTS idx_program_participant_username ON program_participant (username);

-- Program templates
CREATE SEQUENCE IF NOT EXISTS program_template_id_seq;

CREATE TABLE IF NOT EXISTS program_template
(
    id            BIGINT       DEFAULT nextval('program_template_id_seq') NOT NULL PRIMARY KEY,
    company_id    BIGINT,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(500),
    goal          TEXT,
    target_group  VARCHAR(255),
    duration_days INT,
    focus_areas   JSONB        DEFAULT '[]'::jsonb NOT NULL,
    active        BOOLEAN      DEFAULT TRUE NOT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by    VARCHAR(255) NOT NULL,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_program_template_company_id ON program_template (company_id);

-- Predefined focus areas (translatable via i18n key)
-- Custom areas added by users are stored as plain text (no fa. prefix)
CREATE TABLE IF NOT EXISTS focus_area
(
    key VARCHAR(100) NOT NULL PRIMARY KEY
);

-- Program options (visibility & privacy choices HR can toggle)
-- "always on" items are NOT stored here — they are hardcoded on FE
CREATE TABLE IF NOT EXISTS program_option
(
    key      VARCHAR(100) NOT NULL PRIMARY KEY,
    category VARCHAR(30)  NOT NULL,
    always_on   BOOLEAN      NOT NULL DEFAULT FALSE
);

INSERT INTO program_option (key, category, always_on) VALUES
    ('opt.hr.session-attendance',      'HR',      TRUE),
    ('opt.hr.goal-completion',         'HR',      TRUE),
    ('opt.hr.micro-action-rate',       'HR',      FALSE),
    ('opt.hr.checkpoint-responses',    'HR',      FALSE),
    ('opt.hr.assessment-results',      'HR',      FALSE),
    ('opt.mgr.enrollment-status',      'MANAGER', TRUE),
    ('opt.mgr.focus-area-goal',        'MANAGER', FALSE),
    ('opt.mgr.session-attendance',     'MANAGER', FALSE),
    ('opt.mgr.goal-progress',          'MANAGER', FALSE);

INSERT INTO focus_area (key) VALUES
    ('fa.giving-feedback'),
    ('fa.delegation'),
    ('fa.self-awareness'),
    ('fa.strategic-thinking'),
    ('fa.team-motivation'),
    ('fa.communication'),
    ('fa.conflict-resolution'),
    ('fa.time-management'),
    ('fa.emotional-intelligence'),
    ('fa.decision-making');

-- Global program templates (company_id = NULL → available to all)
INSERT INTO program_template (company_id, name, description, goal, target_group, duration_days, focus_areas, created_by)
VALUES
    (NULL, 'template.leadership-90d', 'template.leadership-90d.description',
     'template.leadership-90d.goal', NULL, 90,
     '["fa.giving-feedback", "fa.delegation", "fa.strategic-thinking"]'::jsonb, 'system'),

    (NULL, 'template.stress-resilience-90d', 'template.stress-resilience-90d.description',
     'template.stress-resilience-90d.goal', NULL, 90,
     '["fa.self-awareness", "fa.emotional-intelligence", "fa.time-management"]'::jsonb, 'system'),

    (NULL, 'template.new-manager-90d', 'template.new-manager-90d.description',
     'template.new-manager-90d.goal', NULL, 90,
     '["fa.giving-feedback", "fa.delegation", "fa.team-motivation", "fa.communication"]'::jsonb, 'system');

CREATE INDEX IF NOT EXISTS users_coach_idx ON users(coach);