package com.framework.ui.models;

import com.framework.utils.RandomDataUtil;

/**
 * DESIGN PATTERN: Builder (test data object). Immutable record + fluent builder with sensible random defaults.
 */
public record UserData(String role, String employeeHint, String status, String username, String password) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String role = "ESS";
        private String employeeHint = "a";
        private String status = "Enabled";
        private String username = RandomDataUtil.username("qa_user");
        private String password = RandomDataUtil.password();

        public Builder role(String v) { this.role = v; return this; }
        public Builder employeeHint(String v) { this.employeeHint = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Builder username(String v) { this.username = v; return this; }
        public Builder password(String v) { this.password = v; return this; }

        public UserData build() {
            return new UserData(role, employeeHint, status, username, password);
        }
    }
}
