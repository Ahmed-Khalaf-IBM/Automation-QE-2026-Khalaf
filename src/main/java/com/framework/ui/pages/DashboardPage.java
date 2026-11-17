package com.framework.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DashboardPage extends BasePage {

    private final Locator header = page.locator(".oxd-topbar-header-breadcrumb h6");
    private final Locator sideMenuAdmin = page.locator(".oxd-main-menu-item", new Page.LocatorOptions().setHasText("Admin"));

    public DashboardPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        header.waitFor();
        return text(header).equals("Dashboard");
    }

    public AdminPage goToAdmin() {
        click(sideMenuAdmin);
        AdminPage admin = new AdminPage(page);
        admin.waitUntilLoaded();
        return admin;
    }
}
