-- Participant experience: extend program_participant + add weekly_practice + assessment tables

-- Extend program_participant with enrollment state
ALTER TABLE program_participant
    ADD COLUMN IF NOT EXISTS status       VARCHAR(20)  NOT NULL DEFAULT 'INVITED',
    ADD COLUMN IF NOT EXISTS focus_area   VARCHAR(100) REFERENCES focus_area(key),
    ADD COLUMN IF NOT EXISTS personal_goal TEXT,
    ADD COLUMN IF NOT EXISTS current_cycle INT         NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS enrolled_at  TIMESTAMP;

-- Extend program with participant-facing config
ALTER TABLE program
    ADD COLUMN IF NOT EXISTS allow_full_area_library BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS total_cycles            INT;

-- assessment_question: 5 Likert questions per focus area (50 total, seeded later by content team)
CREATE SEQUENCE IF NOT EXISTS assessment_question_id_seq;

CREATE TABLE IF NOT EXISTS assessment_question
(
    id             BIGINT      DEFAULT nextval('assessment_question_id_seq') NOT NULL PRIMARY KEY,
    focus_area_key VARCHAR(100) NOT NULL REFERENCES focus_area(key),
    question_order INT          NOT NULL CHECK (question_order BETWEEN 1 AND 5),
    question_text  TEXT         NOT NULL,
    CONSTRAINT uk_assessment_question UNIQUE (focus_area_key, question_order)
);

-- weekly_practice: one per week per participant per cycle
CREATE SEQUENCE IF NOT EXISTS weekly_practice_id_seq;

CREATE TABLE IF NOT EXISTS weekly_practice
(
    id               BIGINT    DEFAULT nextval('weekly_practice_id_seq') NOT NULL PRIMARY KEY,
    participant_id   BIGINT    NOT NULL REFERENCES program_participant(id),
    cycle            INT       NOT NULL DEFAULT 1,
    week_number      INT       NOT NULL,
    text             TEXT      NOT NULL,
    source           VARCHAR(20) NOT NULL DEFAULT 'AI',  -- AI / EDITED / CUSTOM
    friday_response  VARCHAR(20),                        -- YES / PARTIAL / NO / null = no response
    blocker_reason   TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_weekly_practice UNIQUE (participant_id, cycle, week_number)
);

CREATE INDEX IF NOT EXISTS idx_weekly_practice_participant ON weekly_practice (participant_id);

-- assessment_response: baseline / mid / final per cycle
CREATE SEQUENCE IF NOT EXISTS assessment_response_id_seq;

CREATE TABLE IF NOT EXISTS assessment_response
(
    id             BIGINT    DEFAULT nextval('assessment_response_id_seq') NOT NULL PRIMARY KEY,
    participant_id BIGINT    NOT NULL REFERENCES program_participant(id),
    type           VARCHAR(20) NOT NULL,   -- BASELINE / MID / FINAL
    cycle          INT         NOT NULL DEFAULT 1,
    focus_area_key VARCHAR(100) NOT NULL REFERENCES focus_area(key),
    q1             INT CHECK (q1 BETWEEN 1 AND 5),
    q2             INT CHECK (q2 BETWEEN 1 AND 5),
    q3             INT CHECK (q3 BETWEEN 1 AND 5),
    q4             INT CHECK (q4 BETWEEN 1 AND 5),
    q5             INT CHECK (q5 BETWEEN 1 AND 5),
    open_text      TEXT,
    nps            INT CHECK (nps BETWEEN 0 AND 10),  -- only on final cycle FINAL assessment
    completed_at   TIMESTAMP,
    CONSTRAINT uk_assessment_response UNIQUE (participant_id, type, cycle)
);

CREATE INDEX IF NOT EXISTS idx_assessment_response_participant ON assessment_response (participant_id);

-- AI prompt for weekly practice generation
INSERT INTO ai_prompt (id, value) VALUES ('WEEKLY_PRACTICE',
'You are an expert leadership coach. Generate exactly 5 distinct weekly practice suggestions for a participant in a corporate coaching program.

Focus area: {focusArea}
Personal goal: {personalGoal}
Program goal: {programGoal}

IMPORTANT: Respond in {language}.

Each practice should:
- Be a concrete, actionable task for one week
- Be specific and measurable (not vague advice)
- Directly relate to the focus area and personal goal
- Start with "This week, ..." or a similar action-oriented phrase
- Be 1-2 sentences maximum

Return ONLY a valid JSON array of 5 strings. No explanations, no numbering, no other text.
Example format: ["Practice 1 text", "Practice 2 text", "Practice 3 text", "Practice 4 text", "Practice 5 text"]');
