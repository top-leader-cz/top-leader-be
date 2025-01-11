package com.topleader.topleader.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgreSQLExtension implements BeforeAllCallback {

    private static PostgreSQLContainer POASTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres").withTag("15-alpine"));

    @Override
    public void beforeAll(ExtensionContext context) {
        if(!POASTGRES.isRunning()) {
            POASTGRES.start();
            System.setProperty("spring.datasource.url", POASTGRES.getJdbcUrl());
            System.setProperty("spring.datasource.username", POASTGRES.getUsername());
            System.setProperty("spring.datasource.password", POASTGRES.getPassword());
        }
    }


}