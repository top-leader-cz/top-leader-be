-- Test data for UserRepository tests

-- Insert test company
INSERT INTO company (id, name) VALUES (100, 'Test Company');

-- Insert test users
INSERT INTO users (id, username, password, first_name, last_name, email, authorities, status, locale, time_zone, company_id)
VALUES (1, 'test.user1@gmail.com', 'password', 'Test', 'User1', 'test.user1@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC', 100),
       (2, 'test.user2@gmail.com', 'password', 'Test', 'User2', 'test.user2@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC', 100),
       (3, 'test.user3@gmail.com', 'password', 'Test', 'User3', 'test.user3@gmail.com', '["USER"]', 'AUTHORIZED', 'en', 'UTC', 100),
       (4, 'test.coach@gmail.com', 'password', 'Test', 'Coach', 'test.coach@gmail.com', '["COACH"]', 'AUTHORIZED', 'en', 'UTC', NULL),
       (5, 'canceled.no.alloc@gmail.com', 'password', 'Canceled', 'NoAlloc', 'canceled.no.alloc@gmail.com', '["USER"]', 'CANCELED', 'en', 'UTC', 100),
       (6, 'canceled.with.alloc@gmail.com', 'password', 'Canceled', 'WithAlloc', 'canceled.with.alloc@gmail.com', '["USER"]', 'CANCELED', 'en', 'UTC', 100);

-- coaching package needed for user_allocation FK
INSERT INTO coaching_package (id, company_id, pool_type, total_units, valid_from, valid_to, status, created_at, created_by, updated_at, updated_by)
VALUES (1, 100, 'CORE', 100, CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP + INTERVAL '1 year', 'ACTIVE', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test');

-- canceled.with.alloc has allocated_units > 0 -> should be visible
-- canceled.no.alloc has no allocation -> should be hidden
INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_at, created_by, updated_at, updated_by)
VALUES (1, 100, 1, 'canceled.with.alloc@gmail.com', 5, 0, 'ACTIVE', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test');
