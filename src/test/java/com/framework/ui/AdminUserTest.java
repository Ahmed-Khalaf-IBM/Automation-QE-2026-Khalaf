package com.framework.ui;

import com.framework.config.ConfigManager;
import com.framework.ui.models.UserData;
import com.framework.ui.pages.AddUserPage;
import com.framework.ui.pages.AdminPage;
import com.framework.ui.pages.DashboardPage;
import com.framework.ui.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AdminUserTest extends BaseUiTest {

    private final ConfigManager cfg = ConfigManager.get();

    @Test(description = "Login as Admin, add a new user in Admin tab, then search for it")
    public void addNewUserAndSearchForIt() {
        DashboardPage dashboard = new LoginPage(page())
                .open(cfg.get("ui.base.url"))
                .loginAs(cfg.get("ui.admin.username"), cfg.get("ui.admin.password"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should load after admin login");

        AdminPage admin = dashboard.goToAdmin();
        UserData newUser = UserData.builder().build();
        AddUserPage addUser = admin.clickAdd();
        addUser.createUser(newUser);

        admin = new AdminPage(page());
        admin.waitUntilLoaded();
        admin.searchByUsername(newUser.username());

        Assert.assertEquals(admin.resultCount(), 1, "Exactly one user expected for " + newUser.username());
        Assert.assertTrue(admin.resultsContain(newUser.username()), "Search result must show the new user");
        Assert.assertTrue(admin.firstRowText().contains(newUser.role()), "Role column should be " + newUser.role());
    }

    @Test(description = "Wrong password shows an error")
    public void invalidLoginShowsError() {
        new LoginPage(page()).open(cfg.get("ui.base.url")).loginAs("Admin", "wrong_pass");
        Assert.assertEquals(new LoginPage(page()).errorMessage(), "Invalid credentials");
    }
}
