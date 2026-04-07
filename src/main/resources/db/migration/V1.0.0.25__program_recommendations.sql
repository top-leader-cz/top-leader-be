-- Program participant recommendations (Learn more section on participant dashboard)
-- Stores AI-generated article + video recommendations per participant per cycle.

CREATE TABLE program_participant_recommendation
(
    id                      BIGSERIAL PRIMARY KEY,
    program_participant_id  BIGINT       NOT NULL REFERENCES program_participant (id) ON DELETE CASCADE,
    cycle                   INTEGER      NOT NULL,
    type                    VARCHAR(20)  NOT NULL,
    content                 JSONB        NOT NULL,
    relevance_rank          INTEGER      NOT NULL,
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_program_participant_recommendation_participant_cycle
    ON program_participant_recommendation (program_participant_id, cycle);

-- Seed dedicated prompts for program recommendations.
-- Start as copies of USER_ARTICLES / USER_PREVIEWS so they can be tuned independently
-- without regressing the Self-Development dashboard.
INSERT INTO ai_prompt (id, value)
SELECT 'PROGRAM_ARTICLE', value FROM ai_prompt WHERE id = 'USER_ARTICLES';

INSERT INTO ai_prompt (id, value)
SELECT 'PROGRAM_VIDEO', value FROM ai_prompt WHERE id = 'USER_PREVIEWS';
