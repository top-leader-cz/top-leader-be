-- Test data for QuestionRepository tests

-- Insert test questions
INSERT INTO fb_question (id, key, default_question)
VALUES (1, 'test.question.1', true),
       (2, 'test.question.2', true),
       (3, 'test.question.custom', false);

-- Reset sequence to start from 4
SELECT setval('fb_question_id_seq', 4, false);
