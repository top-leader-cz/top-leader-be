DO $$
DECLARE
    truncate_sql text;
BEGIN
    SELECT 'TRUNCATE TABLE ' || string_agg(quote_ident(tablename), ', ') || ' CASCADE'
    INTO truncate_sql
    FROM pg_tables
    WHERE schemaname = 'public'
      AND tablename NOT IN ('flyway_schema_history');
    IF truncate_sql IS NOT NULL THEN
        EXECUTE truncate_sql;
    END IF;
END $$;
