package com.framework.api.client;

import com.framework.api.auth.AuthStrategy;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;

import java.util.Map;

/**
 * DESIGN PATTERN: Facade.
 * Hides Playwright's APIRequestContext behind get/post/put/patch/delete and applies the AuthStrategy.
 */
public class ApiClient implements AutoCloseable {

    private final Playwright playwright;
    private final APIRequestContext context;
    private final AuthStrategy auth;

    ApiClient(String baseUrl, AuthStrategy auth) {
        this.playwright = Playwright.create();
        this.auth = auth;
        this.context = playwright.request().newContext(new APIRequest.NewContextOptions().setBaseURL(baseUrl));
    }

    private RequestOptions options(Object body) {
        RequestOptions o = RequestOptions.create().setHeader("Content-Type", "application/json");
        for (Map.Entry<String, String> h : auth.headers().entrySet()) {
            o.setHeader(h.getKey(), h.getValue());
        }
        if (body != null) {
            o.setData(body instanceof String ? body : toJson(body));
        }
        return o;
    }

    private static String toJson(Object body) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot serialise body", e);
        }
    }

    public ApiResponse get(String path) {
        return new ApiResponse(context.get(path, options(null)));
    }

    public ApiResponse post(String path, Object body) {
        return new ApiResponse(context.post(path, options(body)));
    }

    public ApiResponse put(String path, Object body) {
        return new ApiResponse(context.put(path, options(body)));
    }

    public ApiResponse patch(String path, Object body) {
        return new ApiResponse(context.patch(path, options(body)));
    }

    public ApiResponse delete(String path) {
        return new ApiResponse(context.delete(path, options(null)));
    }

    @Override
    public void close() {
        context.dispose();
        playwright.close();
    }
}
