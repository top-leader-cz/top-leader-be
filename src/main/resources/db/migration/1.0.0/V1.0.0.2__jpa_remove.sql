-- Migration for JPA to Spring Data JDBC conversion

-- Badge table - add id column
ALTER TABLE badge DROP CONSTRAINT badge_pkey;
ALTER TABLE badge ADD COLUMN id BIGSERIAL PRIMARY KEY;
CREATE UNIQUE INDEX badge_unique_idx ON badge(username, achievement_type, month, year);
CREATE INDEX badge_username_idx ON badge(username);
