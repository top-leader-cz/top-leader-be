-- =============================================================================
-- Seed: Focus areas + program templates
-- =============================================================================
-- Idempotent - safe to run multiple times.
-- Run via: psql -h localhost -U postgres -d topleader -f misc/seed-focus-areas-and-templates.sql
-- =============================================================================

-- ── Focus areas ──────────────────────────────────────────────────────────────
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
    ('fa.decision-making')
ON CONFLICT (key) DO NOTHING;

-- ── Program options (visibility & privacy) ──────────────────────────────────
INSERT INTO program_option (key, category, always_on) VALUES
    ('opt.hr.session-attendance',      'HR',      TRUE),
    ('opt.hr.goal-completion',         'HR',      TRUE),
    ('opt.hr.micro-action-rate',       'HR',      FALSE),
    ('opt.hr.checkpoint-responses',    'HR',      FALSE),
    ('opt.hr.assessment-results',      'HR',      FALSE),
    ('opt.mgr.enrollment-status',      'MANAGER', TRUE),
    ('opt.mgr.focus-area-goal',        'MANAGER', FALSE),
    ('opt.mgr.session-attendance',     'MANAGER', FALSE),
    ('opt.mgr.goal-progress',          'MANAGER', FALSE)
ON CONFLICT (key) DO NOTHING;

-- ── Program templates (global, company_id = NULL) ────────────────────────────
INSERT INTO program_template (company_id, name, description, goal, target_group, duration_days, focus_areas, created_by)
SELECT NULL, v.name, v.description, v.goal, NULL, v.duration_days, v.focus_areas::jsonb, 'system'
FROM (VALUES
    ('template.leadership-90d',
     'template.leadership-90d.description',
     'template.leadership-90d.goal',
     90,
     '["fa.giving-feedback", "fa.delegation", "fa.strategic-thinking"]'),

    ('template.stress-resilience-90d',
     'template.stress-resilience-90d.description',
     'template.stress-resilience-90d.goal',
     90,
     '["fa.self-awareness", "fa.emotional-intelligence", "fa.time-management"]'),

    ('template.new-manager-90d',
     'template.new-manager-90d.description',
     'template.new-manager-90d.goal',
     90,
     '["fa.giving-feedback", "fa.delegation", "fa.team-motivation", "fa.communication"]')
) AS v(name, description, goal, duration_days, focus_areas)
WHERE NOT EXISTS (
    SELECT 1 FROM program_template pt WHERE pt.name = v.name AND pt.company_id IS NULL
);
