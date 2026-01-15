-- Add DEFAULT values for ID columns to support Spring Data JDBC auto-generation
ALTER TABLE user_message ALTER COLUMN id SET DEFAULT nextval('message_id_seq');
ALTER TABLE user_chat ALTER COLUMN chat_id SET DEFAULT nextval('chat_id_seq');
