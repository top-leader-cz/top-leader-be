INSERT INTO company
VALUES (1, 'Company 1'),
       (2, 'Company 2')
;

INSERT INTO users (username, password, first_name, last_name, authorities, time_zone, status, company_id, coach, credit, requested_credit,
                   is_trial, paid_credit, sum_requested_credit, requested_by, locale)
VALUES ('user1', 'password1', 'John', 'Doe', '["USER"]', 'UTC', 'AUTHORIZED', 1, 'coach1', 100, 50, false, 100, 1000, 'god', 'en'),
       ('coach1', 'password2', 'Jane', 'Smith', '["USER", "COACH"]', 'GMT', 'PENDING', 2, 'coach1', 150, 75, true, 0, 2000, 'god' , 'en'),
       ('user3', 'password3', 'Alice', 'Johnson', '["USER","HR"]', 'EST', 'AUTHORIZED', 1, NULL, 200, 100, false, 1, 3000, 'god', 'en'),
       ('user4', 'password4', 'Bob', 'Brown', '["USER"]', 'PST', 'PENDING', 3, 'coach2', 250, 125, true, 1, 4000, 'god', 'en'),
       ('coach2', 'password5', 'Eve', 'Williams', '["USER", "COACH"]', 'CST', 'AUTHORIZED', 2, 'coach2', 300, 150, false, 1, 500, 'god', 'en'),
       ('hr1', 'password5', 'HrEve', 'Williams', '["USER", "HR"]', 'CST', 'AUTHORIZED', 2, 'coach2', 300, 150, false, 1, 600, 'god', 'en'),
       ('hr2', 'password5', 'HrBob', 'Williams', '["USER", "HR"]', 'CST', 'AUTHORIZED', 2, 'coach2', 300, 150, false, 1, 700, 'somebody', 'en');