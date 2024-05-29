INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id, coach, credit,
                   requested_credit, scheduled_credit, paid_credit)
VALUES ('managerUser', 'password123', '["USER", "MANAGER"]', 'PENDING', 'UTC', 'John', 'Doe', 123, 'Coach1', 100, 0, 10, 111),
       ('user1', 'password456', '["USER"]', 'AUTHORIZED', 'UTC', 'Alice', 'Smith', 123, 'Coach2', 50, 10, 20, 222),
       ('user2', 'password789', '["USER"]', 'AUTHORIZED', 'UTC', 'Bob', 'Johnson', 123, 'Coach3', 75, 25, 30, 333),
       ('Coach2', 'password789', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', 'Coach', 'Borek', 456, null, 75, 25, 30, 333)
;

INSERT INTO users_managers (user_username, manager_username)
VALUES ('user1', 'managerUser'),
       ('user2', 'managerUser');

INSERT INTO user_info(username, strengths, area_of_development, long_term_goal)
VALUES ('user1', '["s1", "s2", "s3", "s4", "s5", "s5"]', '["aod"]', 'Goal1'),
       ('user2', '["s6", "s7", "s8", "s9", "s10", "s1"]', '["Area2"]', 'Goal2');
