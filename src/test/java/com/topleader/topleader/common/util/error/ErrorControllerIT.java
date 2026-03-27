package com.topleader.topleader.common.util.error;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ErrorControllerIT extends IntegrationTest {

    @Test
    @WithMockUser
    void shouldReturnFieldNameWhenStringFieldReceivesObject() throws Exception {
        mvc.perform(post("/api/test/error-controller")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": {"nested": "object"},
                      "entries": []
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                [
                  {
                    "errorCode": "INVALID_INPUT",
                    "fields": [{"name": "name", "value": null}],
                    "errorMessage": "Cannot parse field 'name': expected String"
                  }
                ]
                """));
    }

    @Test
    @WithMockUser
    void shouldReturnNestedFieldPathWhenNestedStringFieldReceivesObject() throws Exception {
        mvc.perform(post("/api/test/error-controller")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "test",
                      "entries": [{"value": {"id": 1}}]
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                [
                  {
                    "errorCode": "INVALID_INPUT",
                    "fields": [{"name": "entries[0].value", "value": null}],
                    "errorMessage": "Cannot parse field 'entries[0].value': expected String"
                  }
                ]
                """));
    }
}
