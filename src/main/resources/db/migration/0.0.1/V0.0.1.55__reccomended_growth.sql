insert into ai_prompt (id, value) values ('RECOMMENDED_GROWTH', 'Based on the user''s profile information, where the company’s business strategy is %s, the current position is %s, and the aspired competency is %s, suggest a few recommended areas for development that would align with both their role and their growth aspirations. Please keep each recommendation concise and focused, providing 2-3 suggested areas for development that would best support the user''s career growth. return response in  %s language return result as json array in the format  use json example bellow as response
    {
        "area": "area"
        "recommendation": "recommendation"
    }'
  );
