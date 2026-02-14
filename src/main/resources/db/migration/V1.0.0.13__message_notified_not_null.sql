UPDATE user_message SET notified = false WHERE notified IS NULL;
ALTER TABLE user_message ALTER COLUMN notified SET NOT NULL;
ALTER TABLE user_message ALTER COLUMN notified SET DEFAULT false;
