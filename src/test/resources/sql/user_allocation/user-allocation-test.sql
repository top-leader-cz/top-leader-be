INSERT INTO company(id, name)
VALUES (1, 'Test Company 1'),
       (2, 'Test Company 2');

SELECT setval('company_id_seq', 2, true);

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES ('hrUser', 'password', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 'HR', 'User', 1),
       ('adminUser', 'password', '["USER", "ADMIN"]', 'AUTHORIZED', 'UTC', 'Admin', 'User', 1),
       ('hrUser2', 'password', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 'HR2', 'User', 2),
       ('user1', 'password', '["USER"]', 'AUTHORIZED', 'UTC', 'User', 'One', 1),
       ('user2', 'password', '["USER"]', 'AUTHORIZED', 'UTC', 'User', 'Two', 1),
       ('user3', 'password', '["USER"]', 'AUTHORIZED', 'UTC', 'User', 'Three', 1);

INSERT INTO coaching_package (id, company_id, pool_type, total_units, valid_from, valid_to, status, context_ref, created_at, created_by, updated_at, updated_by)
VALUES (nextval('coaching_package_id_seq'), 1, 'CORE', 100, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 'ACTIVE', null, '2024-01-01 00:00:00', 'hrUser', '2024-01-01 00:00:00', 'hrUser'),
       (nextval('coaching_package_id_seq'), 1, 'MASTER', 50, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 'INACTIVE', null, '2024-01-01 00:00:00', 'hrUser', '2024-01-01 00:00:00', 'hrUser'),
       (nextval('coaching_package_id_seq'), 2, 'CORE', 200, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 'ACTIVE', null, '2024-01-01 00:00:00', 'hrUser2', '2024-01-01 00:00:00', 'hrUser2');

INSERT INTO user_allocation (id, company_id, package_id, user_id, allocated_units, consumed_units, status, created_at, created_by, updated_at, updated_by)
VALUES (nextval('user_allocation_id_seq'), 1, 1, 'user1', 10, 0, 'ACTIVE', '2024-01-01 00:00:00', 'hrUser', '2024-01-01 00:00:00', 'hrUser'),
       (nextval('user_allocation_id_seq'), 1, 1, 'user2', 20, 5, 'ACTIVE', '2024-01-01 00:00:00', 'hrUser', '2024-01-01 00:00:00', 'hrUser');
