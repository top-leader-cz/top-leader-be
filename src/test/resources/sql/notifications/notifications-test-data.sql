-- Create test notifications for user "testuser1"
INSERT INTO notification (id, username, type, read, context, created_at)
VALUES
    (nextVal('notification_id_seq'), 'testuser1', 'MESSAGE', false, '{"type": "MESSAGE","fromUser": "sender1", "message": "Notification 1"}', '2023-08-01 10:00:00'),
    (nextVal('notification_id_seq'), 'testuser1', 'MESSAGE', true, '{"type": "MESSAGE","fromUser": "sender2", "message": "Notification 2"}', '2023-08-01 11:00:00'),
    (nextVal('notification_id_seq'), 'testuser1', 'MESSAGE', false, '{"type": "MESSAGE","fromUser": "sender3", "message": "Notification 3"}', '2023-08-01 12:00:00');

-- Create test notifications for user "testuser2"
INSERT INTO notification (id, username, type, read, context, created_at)
VALUES
    (nextVal('notification_id_seq'), 'testuser2', 'MESSAGE', false, '{"type": "MESSAGE","fromUser": "sender4", "message": "Notification 4"}', '2023-08-01 13:00:00'),
    (nextVal('notification_id_seq'), 'testuser2', 'MESSAGE', false, '{"type": "MESSAGE","fromUser": "sender5", "message": "Notification 5"}', '2023-08-01 14:00:00');

-- Create test notifications for user "testuser3"
INSERT INTO notification (id, username, type, read, context, created_at)
VALUES
    (nextVal('notification_id_seq'), 'testuser3', 'MESSAGE', true, '{"type": "MESSAGE","fromUser": "sender6", "message": "Notification 6"}', '2023-08-01 15:00:00'),
    (nextVal('notification_id_seq'), 'testuser3', 'MESSAGE', true, '{"type": "MESSAGE","fromUser": "sender7", "message": "Notification 7"}', '2023-08-01 16:00:00');
