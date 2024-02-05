package com.topleader.topleader;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class AiTest extends IntegrationTest {


    @Test
    public void test() throws Exception {
        String contentAsString = mvc.perform(get("/api/public/ai-test"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        log.info(contentAsString);
    }
}
