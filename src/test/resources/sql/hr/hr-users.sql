INSERT INTO users (username, password, enabled, authorities, state, time_zone, first_name, last_name, company_id, coach, credit,
                   requested_credit, is_trial)
VALUES ('hrUser', 'password123', true, '["USER", "HR"]', 'INVITED', 'UTC', 'John', 'Doe', 123, 'Coach1', 100, 0, false),
       ('user1', 'password456', true, '["USER"]', 'CREATED', 'UTC', 'Alice', 'Smith', 123, 'Coach2', 50, 10, true),
       ('user2', 'password789', true, '["USER"]', 'CREATED', 'UTC', 'Bob', 'Johnson', 456, 'Coach3', 75, 25, false)
;
