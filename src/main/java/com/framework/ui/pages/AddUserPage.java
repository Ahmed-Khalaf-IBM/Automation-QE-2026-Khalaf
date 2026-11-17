package com.framework.ui.pages;

import com.framework.ui.models.UserData;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Admin > Add User form. */
public class AddUserPage extends BasePage {

    private final Locator userRole = field("User Role").locator(".oxd-select-text");
    private final Locator status = field("Status").locator(".oxd-select-text");
    private final Locator employeeName = field("Employee Name").locator("input");
    private final Locator username = field("Username").locator("input");
    private final Locator password = field("Password").locator("input").first();
    private final Locator confirmPassword = field("Confirm Password").locator("input");
    private final Locator save = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
    private final Locator dropdownOptions = page.locator(".oxd-select-dropdown .oxd-select-option");
    private final Locator autoCompleteOptions = page.locator(".oxd-autocomplete-dropdown .oxd-autocomplete-option");

    public AddUserPage(Page page) {
        super(page);
        employeeName.waitFor();
    }

    private Locator field(String label) {
        return page.locator(".oxd-input-group").filter(new Locator.FilterOptions()
                .setHas(page.getByText(label, new Page.GetByTextOptions().setExact(true))));
    }

    private void selectDropdown(Locator dropdown, String option) {
        click(dropdown);
        dropdownOptions.filter(new Locator.FilterOptions().setHasText(option)).first().click();
    }

    /** Employee must already exist: type a hint and pick the first suggestion. Returns the chosen name. */
    private String pickEmployee(String hint) {
        type(employeeName, hint);
        autoCompleteOptions.first().waitFor();
        // wait until the "Searching..." placeholder is replaced by real results
        page.waitForCondition(() -> !autoCompleteOptions.first().innerText().contains("Searching"));
        String chosen = text(autoCompleteOptions.first());
        click(autoCompleteOptions.first());
        return chosen;
    }

    /** Fills and saves the form. Returns the employee name that was linked to the user. */
    public String createUser(UserData user) {
        selectDropdown(userRole, user.role());
        String employee = pickEmployee(user.employeeHint());
        selectDropdown(status, user.status());
        type(username, user.username());
        type(password, user.password());
        type(confirmPassword, user.password());
        click(save);
        page.waitForURL("**/admin/viewSystemUsers**");
        return employee;
    }
}
