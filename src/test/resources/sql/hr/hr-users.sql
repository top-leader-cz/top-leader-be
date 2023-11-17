INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id, coach, credit,
                   requested_credit, scheduled_credit, paid_credit, is_trial)
VALUES ('hrUser', 'password123', '["USER", "HR"]', 'PENDING', 'UTC', 'John', 'Doe', 123, 'Coach1', 100, 0, 10, 111, false),
       ('user1', 'password456', '["USER"]', 'AUTHORIZED', 'UTC', 'Alice', 'Smith', 123, 'Coach2', 50, 10, 20, 222, true),
       ('user2', 'password789', '["USER"]', 'AUTHORIZED', 'UTC', 'Bob', 'Johnson', 456, 'Coach3', 75, 25, 30, 333, false)
;
