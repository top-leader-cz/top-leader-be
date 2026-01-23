/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Helper class for API integration testing.
 * Provides fluent methods for common API test operations.
 *
 * @author TopLeader Team
 */
public class ApiTestHelper {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    public ApiTestHelper(MockMvc mvc, ObjectMapper objectMapper) {
        this.mvc = mvc;
        this.objectMapper = objectMapper;
    }

    /**
     * Perform a GET request to the specified URL.
     */
    public ResultActions performGet(String url) throws Exception {
        return mvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Perform a POST request with JSON body.
     */
    public ResultActions performPost(String url, Object body) throws Exception {
        return mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * Perform a POST request without body.
     */
    public ResultActions performPost(String url) throws Exception {
        return mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Perform a PUT request with JSON body.
     */
    public ResultActions performPut(String url, Object body) throws Exception {
        return mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * Perform a DELETE request.
     */
    public ResultActions performDelete(String url) throws Exception {
        return mvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Perform a PATCH request with JSON body.
     */
    public ResultActions performPatch(String url, Object body) throws Exception {
        return mvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * Build a custom request with additional configuration.
     */
    public MockHttpServletRequestBuilder buildRequest(String method, String url) {
        return switch (method.toUpperCase()) {
            case "GET" -> get(url);
            case "POST" -> post(url);
            case "PUT" -> put(url);
            case "DELETE" -> delete(url);
            case "PATCH" -> patch(url);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }
}
