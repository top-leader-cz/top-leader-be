ALTER TABLE user_insight
    ADD COLUMN user_articles TEXT;

INSERT INTO ai_prompt (id, value) VALUES ('USER_ARTICLES', 
'Generate longform article-style summaries based on the following short-term goals: %s.
For each goal, provide at least 3 in-depth article overviews (500–800 words each). 
Each article must be based on a real expert-level publication (e.g. Harvard Business Review, McKinsey Insights, INSEAD, Psychology Today, OECD, Eurofound, etc.). 
Only use sources that are publicly accessible online. 
DO NOT copy the full source article — rewrite the ideas fluently and originally.
Return the result as a valid JSON array with the following structure:
[{
  "title": "string - title of the article",
  "author": "string - name of the original article''s author",
  "source": "string - name of the publication",
  "url": "string - direct URL to the original article",
  "readTime": "string - e.g., ''7 min read'' based on approx. 250 words/min",
  "language": "string - language code (e.g., ''en'', ''cs'')",
  "perex": "string - short 2–3 sentence intro",
  "summaryText": "string - full-length rewritten article summary (500–800 words)",
  "application": "string - how the article applies to the user''s goal",
  "imagePrompt": "string - Professional business illustration , clean design suitable for a leadership article for imagePrompt"
}]
The response language should be: %s');