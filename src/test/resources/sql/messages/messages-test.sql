INSERT INTO user_message (id, user_from, user_to, message_data, displayed, created_at)
VALUES (nextVal('message_id_seq'), 'user1', 'user2', 'Hello from user1 to user2', true, '2023-08-01 10:00:00'),
       (nextVal('message_id_seq'), 'user2', 'user1', 'Hi there! How are you?', false, '2023-08-01 10:05:00'),
       (nextVal('message_id_seq'), 'user1', 'user2', 'Im doing well, thanks ! ', false, '2023-08-01 10:10:00'),
       (nextVal('message_id_seq'), 'user3', 'user1', 'Hello from user3 to user1', false, '2023-08-01 11:00:00'),
       (nextVal('message_id_seq'), 'user3', 'user1', 'Hello from user3 to user1 old', true, '2023-07-01 11:00:00'),
       (nextVal('message_id_seq'), 'user1', 'user3', 'Hello from user1 to user3', true, '2023-08-01 11:00:00');
