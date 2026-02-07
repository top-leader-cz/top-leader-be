ALTER TABLE user_message
    ALTER COLUMN id SET DEFAULT nextval('message_id_seq');
ALTER TABLE notification
    ALTER COLUMN id SET DEFAULT nextval('notification_id_seq');
ALTER TABLE data_history
    ALTER COLUMN id SET DEFAULT nextval('data_history_seq');
ALTER TABLE coach_availability
    ALTER COLUMN id SET DEFAULT nextval('coach_availability_seq');
ALTER TABLE user_action_step
    ALTER COLUMN id SET DEFAULT nextval('user_action_step_seq');
ALTER TABLE user_chat
    ALTER COLUMN chat_id SET DEFAULT nextval('chat_id_seq');
ALTER TABLE company
    ALTER COLUMN id SET DEFAULT nextval('company_id_seq');
ALTER TABLE scheduled_session
    ALTER COLUMN id SET DEFAULT nextval('scheduled_session_id_seq');
ALTER TABLE credit_history
    ALTER COLUMN id SET DEFAULT nextval('credit_history_seq');
ALTER TABLE sync_event
    ALTER COLUMN id SET DEFAULT nextval('sync_event_id_seq');
ALTER TABLE coaching_package
    ALTER COLUMN id SET DEFAULT nextval('coaching_package_id_seq');
ALTER TABLE user_allocation
    ALTER COLUMN id SET DEFAULT nextval('user_allocation_id_seq');
ALTER TABLE user_message
    ALTER COLUMN id SET DEFAULT nextval('message_id_seq');
ALTER TABLE notification
    ALTER COLUMN id SET DEFAULT nextval('notification_id_seq');
ALTER TABLE user_message
    ALTER COLUMN id SET DEFAULT nextval('message_id_seq');
ALTER TABLE user_chat
    ALTER COLUMN chat_id SET DEFAULT nextval('chat_id_seq');

-- Notification module
ALTER TABLE notification
    ALTER COLUMN id SET DEFAULT nextval('notification_id_seq');

-- History module
ALTER TABLE data_history
    ALTER COLUMN id SET DEFAULT nextval('data_history_seq');

-- Coach module
ALTER TABLE coach_availability
    ALTER COLUMN id SET DEFAULT nextval('coach_availability_seq');

-- User action steps
ALTER TABLE user_action_step
    ALTER COLUMN id SET DEFAULT nextval('user_action_step_seq');

-- Company module
ALTER TABLE company
    ALTER COLUMN id SET DEFAULT nextval('company_id_seq');

-- Scheduled sessions
ALTER TABLE scheduled_session
    ALTER COLUMN id SET DEFAULT nextval('scheduled_session_id_seq');

-- Credit module
ALTER TABLE credit_history
    ALTER COLUMN id SET DEFAULT nextval('credit_history_seq');

-- Calendar sync
ALTER TABLE sync_event
    ALTER COLUMN id SET DEFAULT nextval('sync_event_id_seq');

-- Coaching package and allocation
ALTER TABLE coaching_package
    ALTER COLUMN id SET DEFAULT nextval('coaching_package_id_seq');
ALTER TABLE user_allocation
    ALTER COLUMN id SET DEFAULT nextval('user_allocation_id_seq');