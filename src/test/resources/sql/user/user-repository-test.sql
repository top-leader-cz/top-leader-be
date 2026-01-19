-- Test data for UserRepository tests

-- Insert test users
INSERT INTO users (id, username, password, first_name, last_name, email, authorities, status, locale, time_zone)
VALUES (1, 'test.user1@gmail.com', 'password', 'Test', 'User1', 'test.user1@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC'),
       (2, 'test.user2@gmail.com', 'password', 'Test', 'User2', 'test.user2@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC'),
       (3, 'test.user3@gmail.com', 'password', 'Test', 'User3', 'test.user3@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC'),
       (4, 'test.coach@gmail.com', 'password', 'Test', 'Coach', 'test.coach@gmail.com', '["COACH"]', 'AUTHORIZED', 'en', 'UTC');

-- Note: users table uses BIGSERIAL which automatically creates a sequence
-- The sequence name is users_id_seq (table_name + _id_seq), not user_id_seq
-- However, for tests we don't need to reset the sequence as we use explicit IDs
