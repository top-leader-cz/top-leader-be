package com.topleader.topleader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;


/**
 * Created by Jakub krhovják on 2/20/20.
 */
public class AbstractResetDatabaseListener extends AbstractTestExecutionListener {

    @Autowired
    private DataSource dataSource;

    public final int getOrder() {
        return 2001;
    }

    private boolean alreadyCleared = false;

    @Override
    public void beforeTestClass(TestContext testContext) {
        testContext.getApplicationContext()
            .getAutowireCapableBeanFactory()
            .autowireBean(this);
    }



    @Override
    public void prepareTestInstance(@NotNull TestContext testContext) throws Exception {
        if (!alreadyCleared) {
            cleanupDatabase();
            alreadyCleared = true;
        }
    }

    protected void cleanupDatabase() throws SQLException {
        Connection c = dataSource.getConnection();
        Statement s = c.createStatement();

        // Disable FK
        s.execute("SET session_replication_role = replica");

        // Find all tables and truncate them
        Set<String> tables = new HashSet<>();
        ResultSet rs = s.executeQuery("select table_name from INFORMATION_SCHEMA.TABLES where table_schema = 'public'");
        while (rs.next()) {
            tables.add(rs.getString(1));
        }
        rs.close();
        for (String table : tables) {
            s.executeUpdate("TRUNCATE TABLE " + table + " CASCADE");
        }

        // Idem for sequences
        Set<String> sequences = new HashSet<>();
        rs = s.executeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='public'");
        while (rs.next()) {
            sequences.add(rs.getString(1));
        }
        rs.close();
        for (String seq : sequences) {
            s.executeUpdate("ALTER SEQUENCE " + seq + " RESTART WITH 50");
        }

        // Enable FK
        s.execute("SET session_replication_role = 'origin'");
        s.close();
        c.close();
    }
}
