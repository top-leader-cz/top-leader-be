INSERT INTO company
VALUES (1, 'Company 1'),
       (2, 'Company 2')
;

INSERT INTO users (username, password, first_name, last_name, authorities, time_zone, status, company_id, coach, credit, requested_credit,
                   is_trial)
VALUES ('user1', 'password1', 'John', 'Doe', '["USER"]', 'UTC', 'AUTHORIZED', 1, 'coach1', 100, 50, false),
       ('coach1', 'password2', 'Jane', 'Smith', '["USER", "COACH"]', 'GMT', 'PENDING', 2, 'coach1', 150, 75, true),
       ('user3', 'password3', 'Alice', 'Johnson', '["USER","HR"]', 'EST', 'AUTHORIZED', 1, NULL, 200, 100, false),
       ('user4', 'password4', 'Bob', 'Brown', '["USER"]', 'PST', 'PENDING', 3, 'coach2', 250, 125, true),
       ('coach2', 'password5', 'Eve', 'Williams', '["USER", "COACH"]', 'CST', 'AUTHORIZED', 2, 'coach2', 300, 150, false),
       ('hr1', 'password5', 'HrEve', 'Williams', '["USER", "COACH"]', 'CST', 'AUTHORIZED', 2, 'coach2', 300, 150, false),
       ('hr2', 'password5', 'HrBob', 'Williams', '["USER", "COACH"]', 'CST', 'AUTHORIZED', 2, 'coach2', 300, 150, false);