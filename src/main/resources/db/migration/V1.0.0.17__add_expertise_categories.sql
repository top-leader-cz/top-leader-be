-- Expertise category lookup table
CREATE TABLE expertise_category (
    key VARCHAR(50) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    coach_fields JSONB DEFAULT '[]'::jsonb NOT NULL
);

-- Seed 8 categories with mapping to existing coach.fields values
INSERT INTO expertise_category (key, name, coach_fields) VALUES
('ec.leadership', 'Leadership & Management', '["leadership","cross_cultural_leadership","remote_leadership","leading_without_authority","executive","decision_making","delegation","strategic_thinking","teams","management"]'),
('ec.communication', 'Communication & Influence', '["communication","feedback_culture","negotiations","influencing_skills","conflict","coaching_skills_for_managers"]'),
('ec.resilience', 'Resilience & Wellbeing', '["resilience","stress_management","mental_fitness","wellbeing","fitness","health","psychological_safety"]'),
('ec.self-development', 'Self-Development', '["self_leadership","self_criticism","confidence","imposter_syndrome","emotional_intelligence","career","change"]'),
('ec.performance', 'Performance & Business', '["performance","sales","marketing","business","entrepreneurship","management","time_management","finance"]'),
('ec.innovation', 'Innovation & Transformation', '["innovation_and_creativity","transformations","ai_adoption_for_leaders","ai_governance_and_ethics","organizational_development"]'),
('ec.diversity', 'Diversity & Culture', '["diversity","cultural_differences","women","relationships"]'),
('ec.mentoring', 'Mentoring & Facilitation', '["mentorship","facilitation","life"]');

-- Focus area → category mapping (for auto-prefill in wizard)
CREATE TABLE focus_area_category_mapping (
    focus_area_key VARCHAR(100) NOT NULL REFERENCES focus_area(key),
    category_key VARCHAR(50) NOT NULL REFERENCES expertise_category(key),
    PRIMARY KEY (focus_area_key, category_key)
);

INSERT INTO focus_area_category_mapping (focus_area_key, category_key) VALUES
('fa.giving-feedback', 'ec.communication'),
('fa.delegation', 'ec.leadership'),
('fa.self-awareness', 'ec.self-development'),
('fa.strategic-thinking', 'ec.leadership'),
('fa.team-motivation', 'ec.leadership'),
('fa.communication', 'ec.communication'),
('fa.conflict-resolution', 'ec.communication'),
('fa.time-management', 'ec.performance'),
('fa.emotional-intelligence', 'ec.self-development'),
('fa.decision-making', 'ec.leadership');

-- New columns on program for criteria-based coach matching
ALTER TABLE program ADD COLUMN coach_languages JSONB DEFAULT '[]'::jsonb NOT NULL;
ALTER TABLE program ADD COLUMN coach_categories JSONB DEFAULT '[]'::jsonb NOT NULL;
