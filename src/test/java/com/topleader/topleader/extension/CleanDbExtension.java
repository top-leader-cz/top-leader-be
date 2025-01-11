package com.topleader.topleader.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class CleanDbExtension implements AfterEachCallback, BeforeAllCallback {

    private static DataSource DATASOURCE;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if(DATASOURCE == null) {
            DATASOURCE = DataSourceBuilder.create()
                    .url(System.getProperty("spring.datasource.url"))
                    .username(System.getProperty("spring.datasource.username"))
                    .password(System.getProperty("spring.datasource.password"))
                    .build();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws SQLException {
        cleanupDatabase();
    }

    protected void cleanupDatabase() throws SQLException {
        Connection c = DATASOURCE.getConnection();
        Statement s = c.createStatement();

        // Disable FK
        s.execute("SET session_replication_role = replica");

        // Find all tables and truncate them
        Set<String> tables = new HashSet<>();
        ResultSet rs = s.executeQuery("select table_name from INFORMATION_SCHEMA.TABLES where table_schema = 'public' and table_type = 'BASE TABLE'");
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
            s.executeUpdate("ALTER SEQUENCE " + seq + " RESTART WITH 1");
        }

        // Enable FK
        s.execute("SET session_replication_role = 'origin'");
        s.close();
        c.close();
    }

}
