insert into users (username, coach, password, time_zone, first_name, last_name)
values ('user1', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Cool', 'user1'),
       ('user2', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Bad', 'user2'),
       ('user3', 'coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'No', 'user3')
;

INSERT INTO user_chat(chat_id, user1, user2)
VALUES (nextval('chat_id_seq'), 'user1', 'user2'),
       (nextval('chat_id_seq'), 'user3', 'user2'),
       (nextval('chat_id_seq'), 'user3', 'user1');


INSERT INTO user_message (id, chat_id, user_from, user_to, message_data, displayed, created_at)
VALUES (nextval('message_id_seq'), 1, 'user1', 'user2', 'Hello from user1 to user2', true, '2023-08-01 10:00:00'),
       (nextval('message_id_seq'), 1, 'user2', 'user1', 'Hi there! How are you?', false, '2023-08-01 10:05:00'),
       (nextval('message_id_seq'), 1, 'user1', 'user2', 'Im doing well, thanks ! ', false, '2023-08-01 10:10:00'),
       (nextval('message_id_seq'), 3, 'user3', 'user1', 'Hello from user3 to user1', false, '2023-08-01 11:00:00'),
       (nextval('message_id_seq'), 3, 'user3', 'user1', 'Hello from user3 to user1 old', true, '2023-07-01 11:00:00'),
       (nextval('message_id_seq'), 3, 'user1', 'user3', 'Hello from user1 to user3', true, '2023-08-01 11:01:00');

INSERT INTO last_message(chat_id, message_id)
VALUES (1, 3),
       (3, 6);
