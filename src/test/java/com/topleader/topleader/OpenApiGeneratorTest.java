package com.topleader.topleader;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiGeneratorTest extends IntegrationTest {

    @Test
    @WithMockUser
    void generateOpenApiSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn();

        var response = result.getResponse().getContentAsString();

        assertThat(response).contains("openapi:");

        var outputPath = Path.of("src/main/resources/static/openapi.yaml");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, response);

        System.out.println("OpenAPI spec generated: " + outputPath.toAbsolutePath());
    }
}
