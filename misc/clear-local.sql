DO $$
DECLARE
    truncate_sql text;
BEGIN
    -- Exclude flyway_schema_history and Flyway-managed reference tables
    -- (focus_area, expertise_category, focus_area_category_mapping, program_option)
    -- whose rows are owned by migrations and must survive a data clear.
    -- program_template is handled separately below (preserve global rows, delete company-specific ones).
    SELECT 'TRUNCATE TABLE ' || string_agg(quote_ident(tablename), ', ') || ' CASCADE'
    INTO truncate_sql
    FROM pg_tables
    WHERE schemaname = 'public'
      AND tablename NOT IN (
          'flyway_schema_history',
          'focus_area',
          'expertise_category',
          'focus_area_category_mapping',
          'program_option',
          'program_template'
      );
    IF truncate_sql IS NOT NULL THEN
        EXECUTE truncate_sql;
    END IF;
END $$;

-- Remove company-specific templates; keep global ones (company_id IS NULL)
DELETE FROM program_template WHERE company_id IS NOT NULL;
