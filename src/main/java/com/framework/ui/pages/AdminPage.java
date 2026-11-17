package com.framework.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Admin > User Management > Users (search + list). */
public class AdminPage extends BasePage {

    private final Locator title = page.locator(".oxd-topbar-header-breadcrumb");
    private final Locator addButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add"));
    private final Locator searchUsername = page.locator(".oxd-input-group", new Page.LocatorOptions().setHasText("Username"))
            .locator("input").first();
    private final Locator searchButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search"));
    private final Locator rows = page.locator(".oxd-table-body .oxd-table-card");
    private final Locator recordsFound = page.locator("span.oxd-text", new Page.LocatorOptions().setHasText("Record"));

    public AdminPage(Page page) {
        super(page);
    }

    public void waitUntilLoaded() {
        addButton.waitFor();
        title.waitFor();
    }

    public AddUserPage clickAdd() {
        click(addButton);
        return new AddUserPage(page);
    }

    public AdminPage searchByUsername(String username) {
        type(searchUsername, username);
        click(searchButton);
        recordsFound.first().waitFor();
        return this;
    }

    public int resultCount() {
        return rows.count();
    }

    public boolean resultsContain(String username) {
        return rows.filter(new Locator.FilterOptions().setHasText(username)).count() > 0;
    }

    public String firstRowText() {
        return text(rows.first());
    }
}
