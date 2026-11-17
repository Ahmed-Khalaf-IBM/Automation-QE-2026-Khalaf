package com.framework.api.services;

import com.framework.api.client.ApiClient;
import com.framework.api.client.ApiResponse;

import java.util.Map;

/** DESIGN PATTERN: Service Object - one class per API resource, tests never see URLs. */
public class AuthService {
    private final ApiClient client;

    public AuthService(ApiClient client) {
        this.client = client;
    }

    public ApiResponse login(Object username, Object password) {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        if (username != null) body.put("username", username);
        if (password != null) body.put("password", password);
        return client.post("/login", body);
    }
}
