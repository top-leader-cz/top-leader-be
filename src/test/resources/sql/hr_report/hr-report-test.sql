INSERT INTO company(id, name)
VALUES (1, 'Test Company 1'),
       (2, 'Test Company 2');

SELECT setval('company_id_seq', 2, true);

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES ('hrUser', 'password', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 'HR', 'User', 1),
       ('adminUser', 'password', '["USER", "ADMIN"]', 'AUTHORIZED', 'UTC', 'Admin', 'User', 1),
       ('hrUser2', 'password', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 'HR2', 'User', 2),
       ('regularUser', 'password', '["USER"]', 'AUTHORIZED', 'UTC', 'Regular', 'User', 1),
       ('user1', 'password', '["USER"]', 'AUTHORIZED', 'UTC', 'User', 'One', 1),
       ('user2', 'password', '["USER"]', 'AUTHORIZED', 'UTC', 'User', 'Two', 1),
       ('coach', 'password', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', 'Coach', 'User', 1),
       ('canceledNoSession', 'password', '["USER"]', 'CANCELED', 'UTC', 'Canceled', 'NoSession', 1),
       ('canceledWithSession', 'password', '["USER"]', 'CANCELED', 'UTC', 'Canceled', 'WithSession', 1);

-- canceledWithSession has an UPCOMING session -> should still be visible
INSERT INTO scheduled_session (id, coach_username, username, time, status, created_at, updated_at)
VALUES (100, 'coach', 'canceledWithSession', CURRENT_TIMESTAMP + INTERVAL '1 day', 'UPCOMING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO coaching_package (id, company_id, pool_type, total_units, valid_from, valid_to, status, context_ref, created_at, created_by, updated_at, updated_by)
VALUES (nextval('coaching_package_id_seq'), 1, 'CORE', 100, CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP + INTERVAL '1 year', 'ACTIVE', null, CURRENT_TIMESTAMP, 'hrUser', CURRENT_TIMESTAMP, 'hrUser'),
       (nextval('coaching_package_id_seq'), 1, 'MASTER', 50, CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP + INTERVAL '1 year', 'INACTIVE', null, CURRENT_TIMESTAMP, 'hrUser', CURRENT_TIMESTAMP, 'hrUser'),
       (nextval('coaching_package_id_seq'), 2, 'CORE', 200, CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP + INTERVAL '1 year', 'ACTIVE', null, CURRENT_TIMESTAMP, 'hrUser2', CURRENT_TIMESTAMP, 'hrUser2');

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_at, created_by, updated_at, updated_by)
VALUES (nextval('user_allocation_id_seq'), 1, 1, 'user1', 10, 0, 'ACTIVE', CURRENT_TIMESTAMP, 'hrUser', CURRENT_TIMESTAMP, 'hrUser'),
       (nextval('user_allocation_id_seq'), 1, 1, 'user2', 20, 5, 'ACTIVE', CURRENT_TIMESTAMP, 'hrUser', CURRENT_TIMESTAMP, 'hrUser');
