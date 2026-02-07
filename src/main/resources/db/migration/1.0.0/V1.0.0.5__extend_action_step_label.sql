ALTER TABLE user_action_step ALTER COLUMN label TYPE VARCHAR(2000);
UPDATE ai_prompt
SET value = 'Based on the user''s top 5 talents: {strengths}, selected values: {values}, their chosen area for development: {areaOfDevelopment}, and their long-term goal: {longTermGoal}, generate three action items, i.e short-term goals, each should be articulated in a short, concise sentence, ensuring clarity and focus. The goals must be specific, measurable, achievable, relevant, and within a 1-2 week timeline. Provide only the goal statements in brief sentences without additional explanations. The text is to be in {language} language. Use first person when addressing the user.'
WHERE id = 'ACTIONS_STEPS';

