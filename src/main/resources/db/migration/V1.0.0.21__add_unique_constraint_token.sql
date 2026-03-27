-- Remove duplicate tokens (keep the latest one per token+type combination)
DELETE FROM token t1
    USING token t2
WHERE t1.id < t2.id
  AND t1.token = t2.token
  AND t1.type = t2.type;

-- Add unique constraint to prevent concurrent token collisions
ALTER TABLE token ADD CONSTRAINT uq_token_token_type UNIQUE (token, type);
