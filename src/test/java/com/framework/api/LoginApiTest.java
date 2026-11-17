package com.framework.api;

import com.framework.api.client.ApiClient;
import com.framework.api.client.ApiClientFactory;
import com.framework.api.client.ApiClientFactory.Role;
import com.framework.api.client.ApiResponse;
import com.framework.api.services.AuthService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** API #1 under full test: POST /login, many data sets. */
public class LoginApiTest {

    private ApiClient anon;
    private AuthService auth;

    @BeforeClass
    public void setUp() {
        anon = ApiClientFactory.create(Role.ANONYMOUS);
        auth = new AuthService(anon);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        anon.close();
    }

    @DataProvider(name = "validUsers")
    public Object[][] validUsers() {
        return new Object[][]{
                {"admin", "adminpassword"},
                {"user1", "userpassword"},
                {"user2", "userpassword"},
                {"user3", "userpassword"},
                {"user4", "userpassword"},
        };
    }

    @Test(dataProvider = "validUsers")
    public void validLoginReturnsJwt(String user, String pass) {
        ApiResponse r = auth.login(user, pass);
        Assert.assertEquals(r.status(), 200, r.toString());
        String token = r.json().get("token").asText();
        Assert.assertEquals(token.split("\\.").length, 3, "JWT has header.payload.signature");
    }

    // username, password, expected status, expected error text (fragment)
    @DataProvider(name = "invalidLogins")
    public Object[][] invalidLogins() {
        return new Object[][]{
                {"admin", "wrongpassword", 401, "Invalid credentials"},
                {"nosuchuser", "whatever1", 401, "Invalid credentials"},
                {"admin", "AdminPassword", 401, "Invalid credentials"},      // case sensitive
                {"", "adminpassword", 400, "Username cannot be empty"},
                {null, "adminpassword", 400, "Username is required"},
                {"admin", null, 400, "Password is required"},
                {"admin", "", 400, "Password cannot be empty"},
                {"ab", "adminpassword", 400, "minimum length of 3"},
                {"admin", "12345", 400, "minimum length of 6"},
                {"adm!n", "adminpassword", 400, "alphanumeric"},
                {"a".repeat(31), "adminpassword", 400, "maximum length of 30"},
        };
    }

    @Test(dataProvider = "invalidLogins")
    public void invalidLoginIsRejected(String user, String pass, int status, String message) {
        ApiResponse r = auth.login(user, pass);
        Assert.assertEquals(r.status(), status, r.toString());
        Assert.assertTrue(r.errorMessage().contains(message), "expected '" + message + "' in: " + r.body());
        Assert.assertFalse(r.json().has("token"), "no token on failure");
    }

    @Test
    public void tokenGrantsAccessAndGarbageTokenDoesNot() {
        try (ApiClient admin = ApiClientFactory.create(Role.ADMIN);
             ApiClient bad = ApiClientFactory.withToken("not.a.jwt")) {
            Assert.assertEquals(admin.get("/items").status(), 200);
            Assert.assertEquals(bad.get("/items").status(), 401);
        }
    }

    @Test
    public void basicAuthWorksForAdmin() {
        try (ApiClient basic = ApiClientFactory.create(Role.ADMIN_BASIC)) {
            Assert.assertEquals(basic.get("/items").status(), 200);
        }
    }
}
