-- Add primary_role JSON array column to coach table
ALTER TABLE coach ADD COLUMN primary_roles JSONB DEFAULT '["COACH"]';

-- Update existing coaches to have default primary_role
UPDATE coach SET primary_roles = '["COACH"]' WHERE primary_roles IS NULL;
