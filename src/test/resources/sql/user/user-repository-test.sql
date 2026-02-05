-- Test data for UserRepository tests

-- Insert test company
INSERT INTO company (id, name) VALUES (100, 'Test Company');

-- Insert test users
INSERT INTO users (id, username, password, first_name, last_name, email, authorities, status, locale, time_zone, company_id)
VALUES (1, 'test.user1@gmail.com', 'password', 'Test', 'User1', 'test.user1@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC', 100),
       (2, 'test.user2@gmail.com', 'password', 'Test', 'User2', 'test.user2@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC', 100),
       (3, 'test.user3@gmail.com', 'password', 'Test', 'User3', 'test.user3@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC', 100),
       (4, 'test.coach@gmail.com', 'password', 'Test', 'Coach', 'test.coach@gmail.com', '["COACH"]', 'AUTHORIZED', 'en', 'UTC', NULL),
       (5, 'canceled.no.session@gmail.com', 'password', 'Canceled', 'NoSession', 'canceled.no.session@gmail.com', '["USER"]', 'CANCELED', 'en', 'UTC', 100),
       (6, 'canceled.with.upcoming@gmail.com', 'password', 'Canceled', 'WithUpcoming', 'canceled.with.upcoming@gmail.com', '["USER"]', 'CANCELED', 'en', 'UTC', 100),
       (7, 'canceled.with.completed@gmail.com', 'password', 'Canceled', 'WithCompleted', 'canceled.with.completed@gmail.com', '["USER"]', 'CANCELED', 'en', 'UTC', 100);

-- canceled.with.upcoming has UPCOMING session -> should be visible
-- canceled.with.completed has COMPLETED session -> should be visible
-- canceled.no.session has no sessions -> should be hidden
INSERT INTO scheduled_session (id, username, status, created_at, updated_at)
VALUES (1, 'canceled.with.upcoming@gmail.com', 'UPCOMING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'canceled.with.completed@gmail.com', 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'test.user1@gmail.com', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
