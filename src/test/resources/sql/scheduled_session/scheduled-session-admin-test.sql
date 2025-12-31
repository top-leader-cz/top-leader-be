INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name)
VALUES ('adminUser', 'password', '["USER", "ADMIN"]', 'AUTHORIZED', 'UTC', 'Admin', 'User'),
       ('regularUser', 'password', '["USER"]', 'AUTHORIZED', 'UTC', 'Regular', 'User'),
       ('hrUser', 'password', '["USER", "HR"]', 'AUTHORIZED', 'UTC', 'HR', 'User'),
       ('user1', 'password', '["USER"]', 'AUTHORIZED', 'UTC', 'User', 'One'),
       ('coach', 'password', '["USER", "COACH"]', 'AUTHORIZED', 'UTC', 'Coach', 'User');
