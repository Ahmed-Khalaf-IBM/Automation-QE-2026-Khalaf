package com.framework.api.client;

import com.framework.api.auth.AuthStrategy;
import com.framework.api.auth.BasicAuth;
import com.framework.api.auth.BearerTokenAuth;
import com.framework.api.auth.NoAuth;
import com.framework.api.services.AuthService;
import com.framework.config.ConfigManager;

/**
 * DESIGN PATTERN: Factory.
 * Builds a ready-to-use ApiClient for a given role, wiring the right AuthStrategy.
 */
public final class ApiClientFactory {

    public enum Role { ANONYMOUS, ADMIN, CUSTOMER, ADMIN_BASIC }

    private ApiClientFactory() {}

    public static ApiClient create(Role role) {
        ConfigManager cfg = ConfigManager.get();
        String baseUrl = cfg.get("api.base.url");

        return switch (role) {
            case ANONYMOUS -> new ApiClient(baseUrl, new NoAuth());
            case ADMIN -> new ApiClient(baseUrl, bearer(baseUrl, cfg.get("api.admin.username"), cfg.get("api.admin.password")));
            case CUSTOMER -> new ApiClient(baseUrl, bearer(baseUrl, cfg.get("api.customer.username"), cfg.get("api.customer.password")));
            case ADMIN_BASIC -> new ApiClient(baseUrl, new BasicAuth(cfg.get("api.admin.username"), cfg.get("api.admin.password")));
        };
    }

    /** Custom bearer client for any user (e.g. a freshly created customer). */
    public static ApiClient createFor(String username, String password) {
        String baseUrl = ConfigManager.get().get("api.base.url");
        return new ApiClient(baseUrl, bearer(baseUrl, username, password));
    }

    public static ApiClient withToken(String token) {
        return new ApiClient(ConfigManager.get().get("api.base.url"), new BearerTokenAuth(token));
    }

    private static AuthStrategy bearer(String baseUrl, String username, String password) {
        try (ApiClient anon = new ApiClient(baseUrl, new NoAuth())) {
            String token = new AuthService(anon).login(username, password).json().get("token").asText();
            return new BearerTokenAuth(token);
        }
    }
}
