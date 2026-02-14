package com.topleader.topleader;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class ResetDatabaseAfterTestMethodListener extends AbstractTestExecutionListener {

    private static final String CLEANUP_SQL = """
            DO $$
            DECLARE r RECORD;
            BEGIN
              SET session_replication_role = replica;
              FOR r IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename != 'flyway_schema_history' LOOP
                EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE';
              END LOOP;
              FOR r IN SELECT sequencename FROM pg_sequences WHERE schemaname = 'public' LOOP
                EXECUTE 'ALTER SEQUENCE ' || quote_ident(r.sequencename) || ' RESTART WITH 1';
              END LOOP;
              SET session_replication_role = 'origin';
            END $$;
            """;

    private DataSource dataSource;

    @Override
    public final int getOrder() {
        return 2001;
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        dataSource = testContext.getApplicationContext().getBean(DataSource.class);
    }

    @Override
    public void beforeTestMethod(@NotNull TestContext testContext) throws Exception {
        ensureDataSource(testContext);
        cleanupDatabase();
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        ensureDataSource(testContext);
        cleanupDatabase();
    }

    private void ensureDataSource(TestContext testContext) {
        if (dataSource == null) {
            dataSource = testContext.getApplicationContext().getBean(DataSource.class);
        }
    }

    private void cleanupDatabase() throws SQLException {
        try (var c = dataSource.getConnection(); var s = c.createStatement()) {
            s.execute(CLEANUP_SQL);
        }
    }
}
