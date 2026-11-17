package com.framework.api.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/** HTTP Basic - the capstone API accepts it for admin users only. */
public class BasicAuth implements AuthStrategy {
    private final String encoded;

    public BasicAuth(String username, String password) {
        this.encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Map<String, String> headers() {
        return Map.of("Authorization", "Basic " + encoded);
    }
}
