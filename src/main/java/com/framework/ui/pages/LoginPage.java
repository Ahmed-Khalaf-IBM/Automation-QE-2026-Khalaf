package com.framework.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LoginPage extends BasePage {

    private final Locator username = page.getByPlaceholder("Username");
    private final Locator password = page.getByPlaceholder("Password");
    private final Locator loginButton = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Login"));
    private final Locator error = page.locator(".oxd-alert-content-text");

    public LoginPage(Page page) {
        super(page);
    }

    public LoginPage open(String baseUrl) {
        page.navigate(baseUrl + "/web/index.php/auth/login");
        username.waitFor();
        return this;
    }

    /** Fluent: returns the next page object the user lands on. */
    public DashboardPage loginAs(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginButton);
        return new DashboardPage(page);
    }

    public String errorMessage() {
        error.waitFor();
        return text(error);
    }
}
