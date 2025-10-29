update coach set public_profile  = false where public_profile is null;
ALTER TABLE coach
  ALTER COLUMN public_profile SET DEFAULT false,
  ALTER COLUMN public_profile SET NOT NULL;

