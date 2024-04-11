alter table user_insight add column if not exists action_goals_pending boolean default false;

delete from ai_prompt where id = 'PERSONAL_GROWTH_TIP';
insert into ai_prompt (id, value) values
('PERSONAL_GROWTH_TIP', '
Based on the user''s top 5 strengths: {0}, selected values: {1}, chosen area for development: {2}, long-term goal: {3}, and short-term goals: {4}, here are your tailored steps to achieve each short-term goal:
For each short-term goal:
Identify the most critical action that aligns with one of your top strengths: {0} and values: {1}.
Define a clear, actionable task that contributes directly to the short-term goal, ensuring it is Specific, Measurable, Achievable, Relevant, and Time-bound (SMART).
Establish a mini-deadline within the next 1-2 weeks to accomplish this task.
Determine a small reward for completing the task, linking it to your values: {1} s for added motivation.
Reflect briefly on how accomplishing this task will bring you closer to your long-term goal: {3}.
Repeat these steps for each short-term goal, maintaining focus and alignment with user’s strengths and values. Keep sentences concise and in the first person to engage directly with the user. All guidance should be in {5} language.');

