UPDATE ai_prompt SET value = 'You must return ONLY valid JSON array. No explanations, no introductory text, no concluding remarks.
Start immediately with [ and end with ]. Do not include any text before or after the JSON.

Generate microlearning content specifically related to the following short-term goals selected by the user. For each short-term goal, provide at least 3 relevant videos from reputable sources such as TED, YouTube, university channels, or well-known educational websites. Ideally search first this channel: https://www.youtube.com/@TED. Ensure the URLs are valid and lead directly to the content. The URLs must be explicitly shown as text and not embedded as clickable links. All videos should be in English. Return in total 7 results. Only focus on what is explicitly asked in the instructions.

Short-term-goals: {actionSteps}

FORBIDDEN - Do NOT write:
- "Here is a JSON array..."
- "```json"
- "These videos cover..."
- Any wrapper text

Return result as json array using this structure (replace with actual content relevant to the goals):
[
  {{
    "title": "actual video title relevant to the goals",
    "url": "actual youtube url",
    "length": "actual video length"
  }}
]'
WHERE id = 'USER_PREVIEWS';

UPDATE ai_prompt SET value = 'Based on the following feedback responses for leadership skills, please summarize the strong areas and areas for improvement. The feedback responses are provided for each question. Each respondent answered the same set of questions. The questions and corresponding responses are as follows.
{resultJson}
Please provide a concise summary of:
Strong areas
Areas for improvement

please use json format like below
{{
  "strongAreas" : "your response"
  "areasOfImprovement: "your response"
}}

Use first person when writing the summary.
Use {language} language. Do not translate json keys "strongAreas" and "areasOfImprovement" json keys'
WHERE id = 'FEEDBACK_SUMMARY';

UPDATE ai_prompt SET value = 'Based on the user''s top 5 talents: {strengths}, selected values: {values}, and their chosen area for development: {areaOfDevelopment}, generate five long-term goals. Each goal should be articulated in a short, concise sentence, ensuring clarity and focus. The goals must be specific, measurable, achievable, relevant, and within a 3-6 month timeline. Provide only the goal statements in brief sentences without additional explanations. The text is to be in {language} language. Use first person when addressing the user.'
WHERE id = 'LONG_TERM_GOALS';

UPDATE ai_prompt SET value = 'Given a user''s top 5 talents: {strengths}, and key values: {values}, provide a comprehensive yet concise leadership style analysis. Highlight how their unique strengths and values combine to shape their approach to leadership. The analysis should be straightforward, relevant, and resonate with users of varying backgrounds. Aim for an output that is inspiring and provides clear direction on how they can apply their talents and values in their leadership role. Keep the analysis under 1000 characters.

Important:

The entire text, including talent and value descriptors, must only be written in the target language {language}.

Do not translate attributes or values literally or mechanically. Instead, rephrase each attribute into natural, commonly used, and stylistically appropriate expressions in the target language that clearly convey the original meaning.

Always use second-person when addressing the user. If the target language is Czech, use informal tykání instead of formal vykání.'
WHERE id = 'LEADERSHIP_STYLE';

UPDATE ai_prompt SET value = 'Create a fun and engaging ''Animal Spirit Guide'' analysis for a user based on their top 5 talents: {strengths}, and key values: {values}. The analysis should metaphorically link these attributes to an animal known for similar characteristics, providing a brief explanation of the connection. The content should be enlightening, fostering a deeper connection with their leadership style in an enjoyable manner. Ensure the description is succinct, clear, and does not exceed 600 characters.

Important:

The entire text, including talent and value descriptors, must only be written in the target language ({language}).

Do not translate attributes or values literally or mechanically. Instead, rephrase each attribute into natural, commonly used, and stylistically appropriate expressions that clearly convey the original meaning.

Choose animals whose described traits align realistically and biologically with actual animal behavior.

Always use second-person when addressing the user. If the target language is Czech, use informal tykání instead of formal vykání.'
WHERE id = 'ANIMAL_SPIRIT';

UPDATE ai_prompt SET value = 'Create a personalized development plan based on the provided information. Follow these instructions precisely and do not add any extra content or suggestions beyond what is requested.
User Information: Top Five Talents of the user are {strengths}, Values are: {values} Area for Development is: {areaOfDevelopment} Long-term Goal is: {longTermGoal} Short-Term Goals are: {actionSteps}
For each short-term goal, provide the following:
Link the short-term goal to one of the user''s talents and values, emphasizing personal alignment.
Formulate a specific, actionable step that adheres to SMART criteria: Specific, Measurable, Achievable, Relevant, and Time-bound.
Assign a mini-deadline within the upcoming 1-2 weeks to complete the step.
Suggest a small, relevant reward connected to the values of the user for task completion. Encourage reflection on how this step contributes to the long-term objective.
Important:
Do not provide additional short-term goals.
Do not suggest or recommend any other steps, plans, or ideas.
Only focus on what is explicitly asked in the instructions.
The text is to be in {language} language.
Use first person when addressing the user.'
WHERE id = 'PERSONAL_GROWTH_TIP';

UPDATE ai_prompt SET value = 'Given a user''s top 5 talents: {strengths}, and key values: {values}, from a diverse pool of 20 talents and 30 values, identify three world leaders who most closely exemplify these unique characteristics. Ensure that these leaders represent diverse backgrounds and reflect the distinctiveness of the user''s profile. Avoid recurring suggestions such as Nelson Mandela, Angela Merkel, and Jacinda Ardern unless they are the most accurate matches. If exact matches are challenging, select leaders who align most closely with the user''s strengths and values. Provide a concise overview of their key leadership traits, main achievements, and resonance with the user''s profile, avoiding dictators or controversial figures. Recommend a widely recognized book, documentary, or article for each leader, mentioning the title and encouraging a URL search. The descriptions should be accurate, embellishment-free, respectful, and culturally appropriate. The response should be within an 800-character limit, addressed in second person.

Important:

The entire text, including talent and value descriptors, must only be written in the target language ({language}).

Do not translate attributes or values literally or mechanically. Instead, rephrase each attribute into natural, commonly used, and stylistically appropriate expressions in the target language that clearly convey the original meaning.

Always use second-person when addressing the user. If the target language is Czech, use informal tykání instead of formal vykání.'
WHERE id = 'WORLD_LEADER_PERSONA';
