-- Add last login tracking
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

-- Program sequences
CREATE SEQUENCE IF NOT EXISTS program_id_seq;
CREATE SEQUENCE IF NOT EXISTS program_participant_id_seq;

-- Program: 1-1 superset of coaching_package, adds name and milestone
CREATE TABLE IF NOT EXISTS program
(
    id                  BIGINT       DEFAULT nextval('program_id_seq') NOT NULL PRIMARY KEY,
    coaching_package_id BIGINT       NOT NULL UNIQUE REFERENCES coaching_package (id),
    name                VARCHAR(255) NOT NULL,
    milestone_date      TIMESTAMP,
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP         NOT NULL,
    created_by          VARCHAR(255) NOT NULL,
    updated_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_program_coaching_package_id ON program (coaching_package_id);

-- Per-participant status within a program
CREATE TABLE IF NOT EXISTS program_participant
(
    id             BIGINT       DEFAULT nextval('program_participant_id_seq') NOT NULL PRIMARY KEY,
    program_id     BIGINT       NOT NULL REFERENCES program (id),
    username       VARCHAR(255) NOT NULL,
    coach_username VARCHAR(255),
    status         VARCHAR(50)  NOT NULL DEFAULT 'ON_TRACK',
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP                     NOT NULL,
    created_by     VARCHAR(255) NOT NULL,
    CONSTRAINT uk_program_participant UNIQUE (program_id, username)
);

CREATE INDEX IF NOT EXISTS idx_program_participant_program_id ON program_participant (program_id);
