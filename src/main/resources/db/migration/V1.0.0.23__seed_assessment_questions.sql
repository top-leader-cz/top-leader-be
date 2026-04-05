-- Seed assessment questions for 12 focus areas (5 Likert questions each)
-- Source: topleader-assessment-seed-data.json v2 final

-- Add missing focus areas
INSERT INTO focus_area (key) VALUES ('fa.resilience'), ('fa.accountability')
ON CONFLICT (key) DO NOTHING;

-- Add facet column (internal label for reporting, not shown to participant)
ALTER TABLE assessment_question ADD COLUMN IF NOT EXISTS facet VARCHAR(100);

-- Update program templates to match full spec focus area mappings
UPDATE program_template SET focus_areas = '["fa.giving-feedback","fa.delegation","fa.strategic-thinking","fa.communication","fa.team-motivation","fa.emotional-intelligence","fa.self-awareness","fa.conflict-resolution","fa.decision-making","fa.accountability"]'::jsonb
WHERE name = 'template.leadership-90d';

UPDATE program_template SET focus_areas = '["fa.giving-feedback","fa.delegation","fa.communication","fa.team-motivation","fa.time-management","fa.accountability"]'::jsonb
WHERE name = 'template.new-manager-90d';

UPDATE program_template SET focus_areas = '["fa.resilience","fa.emotional-intelligence","fa.self-awareness","fa.time-management","fa.communication"]'::jsonb
WHERE name = 'template.stress-resilience-90d';

-- 1. Giving feedback
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.giving-feedback', 1, 'I give specific, behavior-based feedback rather than general comments like "good job" or "that wasn''t great."', 'Specificity'),
    ('fa.giving-feedback', 2, 'I deliver constructive feedback within 48 hours of the event, rather than saving it for a formal review.', 'Timeliness'),
    ('fa.giving-feedback', 3, 'When giving critical feedback, I balance it with genuine recognition of what the person does well.', 'Balance'),
    ('fa.giving-feedback', 4, 'I ask for the other person''s perspective before or after sharing my feedback.', 'Two-way dialogue'),
    ('fa.giving-feedback', 5, 'I follow up after giving feedback to check whether it was understood and actionable.', 'Follow-through');

-- 2. Delegation
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.delegation', 1, 'I delegate tasks based on team members'' development goals, not just on who can do it fastest.', 'Growth-oriented assignment'),
    ('fa.delegation', 2, 'When I delegate, I clearly communicate the desired outcome and deadline, but let the person choose their approach.', 'Outcome clarity'),
    ('fa.delegation', 3, 'I resist the urge to take back a task when someone is struggling, and instead offer support.', 'Trust under pressure'),
    ('fa.delegation', 4, 'I set up check-in points when delegating complex tasks so I can course-correct early without micromanaging.', 'Structured oversight'),
    ('fa.delegation', 5, 'After a delegated task is complete, I give the person credit publicly for their work.', 'Credit sharing');

-- 3. Strategic thinking
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.strategic-thinking', 1, 'I regularly set aside time to think about where my team or project should be in 6–12 months, beyond day-to-day tasks.', 'Future orientation'),
    ('fa.strategic-thinking', 2, 'I connect daily decisions to our longer-term goals, and I can explain that connection to my team.', 'Goal alignment'),
    ('fa.strategic-thinking', 3, 'I say no to tasks or projects that don''t align with our current priorities, even when they seem urgent.', 'Prioritization'),
    ('fa.strategic-thinking', 4, 'I challenge assumptions in my team''s plans by asking ''what if this isn''t true?''', 'Critical examination'),
    ('fa.strategic-thinking', 5, 'I can explain our team''s strategic priorities clearly enough that any team member could repeat them.', 'Strategy communication');

-- 4. Communication
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.communication', 1, 'In conversations, I listen fully before formulating my response rather than planning what to say while the other person speaks.', 'Active listening'),
    ('fa.communication', 2, 'I adapt my communication style based on who I''m talking to — their role, context, and preferences.', 'Audience adaptation'),
    ('fa.communication', 3, 'When explaining complex topics, I check understanding by asking the listener to summarize in their own words.', 'Clarity verification'),
    ('fa.communication', 4, 'I initiate difficult conversations rather than avoiding or delaying them.', 'Courage'),
    ('fa.communication', 5, 'My written messages make the key point clear in the first few lines.', 'Written clarity');

-- 5. Team motivation
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.team-motivation', 1, 'I know what motivates each of my team members individually and use that knowledge in how I lead them.', 'Individual awareness'),
    ('fa.team-motivation', 2, 'I help my team see how their work contributes to something meaningful beyond our immediate goals.', 'Purpose connection'),
    ('fa.team-motivation', 3, 'I celebrate small wins and progress, not just final results.', 'Progress recognition'),
    ('fa.team-motivation', 4, 'I give my team members autonomy in how they approach their work, within agreed boundaries.', 'Empowerment'),
    ('fa.team-motivation', 5, 'When someone shares a mistake or concern, I respond with curiosity before judgment.', 'Safety-creating behavior');

-- 6. Time management
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.time-management', 1, 'At the start of each week, I identify my top 3 priorities and protect time for them.', 'Weekly prioritization'),
    ('fa.time-management', 2, 'I decline or shorten meetings that don''t have a clear purpose or where my presence isn''t necessary.', 'Meeting discipline'),
    ('fa.time-management', 3, 'I batch similar tasks together and protect uninterrupted focus blocks in my calendar.', 'Deep work'),
    ('fa.time-management', 4, 'I distinguish between truly urgent tasks and tasks that only feel urgent, and act accordingly.', 'Urgency filtering'),
    ('fa.time-management', 5, 'Before accepting new work, I review my current commitments so I do not overcommit.', 'Capacity management');

-- 7. Emotional intelligence
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.emotional-intelligence', 1, 'I can name the emotion I''m feeling in the moment, even during stressful situations.', 'Emotional awareness'),
    ('fa.emotional-intelligence', 2, 'When I feel frustrated or angry at work, I pause before responding rather than reacting immediately.', 'Self-regulation'),
    ('fa.emotional-intelligence', 3, 'I notice changes in my team members'' mood or energy and check in with them about it.', 'Empathic observation'),
    ('fa.emotional-intelligence', 4, 'I express my own concerns or uncertainties to my team when appropriate, rather than always appearing unaffected.', 'Authentic expression'),
    ('fa.emotional-intelligence', 5, 'In tense conversations, I notice when I become defensive and refocus on understanding the other person.', 'Defensive awareness');

-- 8. Self-awareness
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.self-awareness', 1, 'I can clearly articulate my top 3 strengths and the situations where they become weaknesses.', 'Strengths/shadows'),
    ('fa.self-awareness', 2, 'I actively seek out perspectives from people who see things differently than I do.', 'Blind spot seeking'),
    ('fa.self-awareness', 3, 'After important meetings or decisions, I take a moment to reflect on what went well and what I''d do differently.', 'Reflective practice'),
    ('fa.self-awareness', 4, 'When I receive critical feedback, I focus on understanding it before explaining or defending myself.', 'Feedback absorption'),
    ('fa.self-awareness', 5, 'I notice when my behavior doesn''t align with my stated values and take steps to correct it.', 'Values-behavior alignment');

-- 9. Conflict resolution
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.conflict-resolution', 1, 'When I notice tension between team members, I address it directly rather than hoping it resolves on its own.', 'Early intervention'),
    ('fa.conflict-resolution', 2, 'In a disagreement, I can restate the other person''s position accurately before presenting my own.', 'Understanding before advocating'),
    ('fa.conflict-resolution', 3, 'I focus on shared interests and goals when resolving conflicts, rather than on who is right.', 'Interest-based resolution'),
    ('fa.conflict-resolution', 4, 'When a conversation becomes heated, I lower the emotional intensity instead of matching it.', 'De-escalation'),
    ('fa.conflict-resolution', 5, 'After resolving a conflict, I follow up to ensure the agreed solution is actually working for both parties.', 'Resolution follow-through');

-- 10. Resilience
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.resilience', 1, 'After a setback or failure, I actively identify what I can learn from it rather than dwelling on what went wrong.', 'Growth framing'),
    ('fa.resilience', 2, 'I have specific daily or weekly practices that help me manage stress (exercise, meditation, journaling, etc.).', 'Stress routines'),
    ('fa.resilience', 3, 'I ask for help or support when I''m overwhelmed, rather than trying to handle everything alone.', 'Help-seeking'),
    ('fa.resilience', 4, 'When facing a difficult period, I remind myself that it is temporary and focus on what I can control.', 'Perspective maintenance'),
    ('fa.resilience', 5, 'I deliberately schedule time for activities that restore my energy, and I protect that time.', 'Energy renewal');

-- 11. Decision making
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.decision-making', 1, 'Before making an important decision, I seek input from people with different perspectives and expertise.', 'Input gathering'),
    ('fa.decision-making', 2, 'I set a clear deadline for decisions to avoid analysis paralysis, even when I don''t have perfect information.', 'Decisiveness'),
    ('fa.decision-making', 3, 'After making a decision, I communicate the rationale to those affected — not just the outcome.', 'Transparency'),
    ('fa.decision-making', 4, 'I revisit past decisions to learn from outcomes, without second-guessing every choice.', 'Reflective review'),
    ('fa.decision-making', 5, 'When new information contradicts my decision, I change course rather than defending my original choice.', 'Adaptability');

-- 12. Accountability
INSERT INTO assessment_question (focus_area_key, question_order, question_text, facet) VALUES
    ('fa.accountability', 1, 'When I commit to a deadline or deliverable, I consistently follow through or proactively renegotiate if circumstances change.', 'Commitment reliability'),
    ('fa.accountability', 2, 'I openly acknowledge my mistakes to my team rather than minimizing or deflecting them.', 'Ownership of errors'),
    ('fa.accountability', 3, 'I set clear expectations with my team about what "done" looks like, so there''s no ambiguity.', 'Expectation clarity'),
    ('fa.accountability', 4, 'I hold my team members to their commitments in a way that is firm but respectful.', 'Holding others accountable'),
    ('fa.accountability', 5, 'I regularly review my own goals and commitments and share progress updates without being asked.', 'Proactive transparency');
