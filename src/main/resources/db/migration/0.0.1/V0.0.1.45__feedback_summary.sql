insert into ai_prompt (id, value) values ('FEEDBACK_SUMMARY', 'Based on the following feedback responses for leadership skills, please summarize the strong areas and areas for improvement. The feedback responses are provided for each question. Each respondent answered the same set of questions. The questions and corresponding responses are as follows.
%s
Please provide a concise summary of:
Strong areas
Areas for improvement

please use json format like below
{
  “strongAreas“ : “your response”
  “areasOfImprovement: “your response“
}

Use first person when writing the summary.
Use %s language. Do not translate json keys "strongAreas" and "areasOfImprovement" json keys');