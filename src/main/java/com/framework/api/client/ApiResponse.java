package com.framework.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;

/** Reads the Playwright response once and exposes status / body / typed mapping. */
public class ApiResponse {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final int status;
    private final String body;

    public ApiResponse(APIResponse response) {
        this.status = response.status();
        this.body = response.text();
    }

    public int status() {
        return status;
    }

    public String body() {
        return body;
    }

    public JsonNode json() {
        try {
            return MAPPER.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Response is not JSON (status " + status + "): " + body, e);
        }
    }

    public <T> T as(Class<T> type) {
        try {
            return MAPPER.readValue(body, type);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot map to " + type.getSimpleName() + ": " + body, e);
        }
    }

    public <T> T as(com.fasterxml.jackson.core.type.TypeReference<T> type) {
        try {
            return MAPPER.readValue(body, type);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot map response: " + body, e);
        }
    }

    /** The capstone API reports problems as {"error": "..."} (docs say "errors": [...] - actual behaviour wins). */
    public String errorMessage() {
        JsonNode n = json();
        return n.has("error") ? n.get("error").asText() : n.toString();
    }

    @Override
    public String toString() {
        return status + " " + body;
    }
}
