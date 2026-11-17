package com.framework.api.auth;

import java.util.Map;

/**
 * DESIGN PATTERN: Strategy.
 * Each way of authenticating a request is an interchangeable strategy. The client just asks for headers.
 */
public interface AuthStrategy {
    Map<String, String> headers();
}
