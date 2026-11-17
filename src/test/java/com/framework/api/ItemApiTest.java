package com.framework.api;

import com.framework.api.builders.ItemBuilder;
import com.framework.api.client.ApiClient;
import com.framework.api.client.ApiClientFactory;
import com.framework.api.client.ApiClientFactory.Role;
import com.framework.api.client.ApiResponse;
import com.framework.api.models.Item;
import com.framework.api.services.ItemService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** API #2 under full test: /items (create, read, update, delete, authorisation, validation). */
public class ItemApiTest {

    private ApiClient admin, customer, anon;
    private ItemService adminItems, customerItems, anonItems;
    private final List<String> created = new ArrayList<>();

    @BeforeClass
    public void setUp() {
        admin = ApiClientFactory.create(Role.ADMIN);
        customer = ApiClientFactory.create(Role.CUSTOMER);
        anon = ApiClientFactory.create(Role.ANONYMOUS);
        adminItems = new ItemService(admin);
        customerItems = new ItemService(customer);
        anonItems = new ItemService(anon);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        created.forEach(adminItems::delete);
        admin.close();
        customer.close();
        anon.close();
    }

    private Item createValid() {
        ApiResponse r = adminItems.create(new ItemBuilder().build());
        Assert.assertEquals(r.status(), 201, r.toString());
        Item i = r.as(Item.class);
        created.add(i.id);
        return i;
    }

    // ---------- CREATE : valid data ----------
    // name, description, price, stock
    @DataProvider(name = "validItems")
    public Object[][] validItems() {
        return new Object[][]{
                {"Abc", "Minimum name length", 0.01, 0},                 // lower boundaries
                {"Laptop Pro 15", "High performance laptop", 1200.00, 50},
                {"Mouse", "Wireless, ergonomic (2.4GHz)!", 25.5, 200},
                {"N".repeat(100), "Max name length 100", 5, 1},          // upper boundary on name
                {"Big Stock", "Large stock value here", 9.99, 1_000_000},
        };
    }

    @Test(dataProvider = "validItems")
    public void createItemWithValidData(String name, String desc, double price, int stock) {
        ApiResponse r = adminItems.create(new ItemBuilder().name(name).description(desc).price(price).stock(stock).build());
        Assert.assertEquals(r.status(), 201, r.toString());
        Item i = r.as(Item.class);
        created.add(i.id);
        Assert.assertEquals(i.name, name);
        Assert.assertEquals(i.price, price, 0.001);
        Assert.assertEquals(i.stock, stock);

        Item fetched = adminItems.get(i.id).as(Item.class);   // persisted
        Assert.assertEquals(fetched.name, name);
    }

    // ---------- CREATE : invalid data ----------
    // payload, expected message fragment
    @DataProvider(name = "invalidItems")
    public Object[][] invalidItems() {
        return new Object[][]{
                {new ItemBuilder().name("ab").build(), "minimum length of 3"},
                {new ItemBuilder().name("N".repeat(101)).build(), "maximum length of 100"},
                {new ItemBuilder().name("Bad@Name!").build(), "alphanumeric"},
                {new ItemBuilder().name("").build(), "cannot be empty"},
                {new ItemBuilder().without("name").build(), "name is required"},
                {new ItemBuilder().description("abc").build(), "minimum length of 5"},
                {new ItemBuilder().description("bad <script>").build(), "invalid characters"},
                {new ItemBuilder().price(0).build(), "positive"},
                {new ItemBuilder().price(-5).build(), "positive"},
                {new ItemBuilder().price("free").build(), "must be a number"},
                {new ItemBuilder().without("price").build(), "Price is required"},
                {new ItemBuilder().stock(-1).build(), "cannot be negative"},
                {new ItemBuilder().stock(1.5).build(), "must be an integer"},
                {new ItemBuilder().stock("ten").build(), "must be an integer"},
                {new ItemBuilder().without("stock").build(), "Stock is required"},
        };
    }

    @Test(dataProvider = "invalidItems")
    public void createItemWithInvalidDataIsRejected(Map<String, Object> payload, String message) {
        ApiResponse r = adminItems.create(payload);
        Assert.assertEquals(r.status(), 400, r.toString());
        Assert.assertTrue(r.errorMessage().toLowerCase().contains(message.toLowerCase()),
                "expected '" + message + "' in: " + r.body());
    }

    // ---------- AUTHORISATION ----------
    @Test
    public void customerCannotCreateUpdateOrDeleteItems() {
        Item i = createValid();
        Assert.assertEquals(customerItems.create(new ItemBuilder().build()).status(), 403);
        Assert.assertEquals(customerItems.update(i.id, Map.of("price", 1)).status(), 403);
        Assert.assertEquals(customerItems.delete(i.id).status(), 403);
        Assert.assertEquals(adminItems.get(i.id).status(), 200, "item untouched");
    }

    @Test
    public void customerCanReadItems() {
        Item i = createValid();
        Assert.assertEquals(customerItems.list().status(), 200);
        Assert.assertEquals(customerItems.get(i.id).status(), 200);
    }

    @Test
    public void anonymousIsRejected() {
        Assert.assertEquals(anonItems.list().status(), 401);
        Assert.assertEquals(anonItems.create(new ItemBuilder().build()).status(), 401);
    }

    // ---------- READ ----------
    @Test
    public void getUnknownItemReturns404() {
        ApiResponse r = adminItems.get("999999999");
        Assert.assertEquals(r.status(), 404, r.toString());
        Assert.assertTrue(r.errorMessage().contains("not found"));
    }

    @Test
    public void listContainsCreatedItem() {
        Item i = createValid();
        Assert.assertTrue(adminItems.list().body().contains(i.id));
    }

    // ---------- UPDATE ----------
    // NOTE: docs/API_REFERENCE.md says PUT accepts partial bodies, but the real API validates with the full
    // item schema, so PUT behaves as a full replace. Tests below assert the ACTUAL behaviour.
    // field to change, new value
    @DataProvider(name = "updates")
    public Object[][] updates() {
        return new Object[][]{
                {"name", "Renamed Item"},
                {"price", 12.34},
                {"stock", 77},
                {"description", "A new description"},
        };
    }

    @Test(dataProvider = "updates")
    public void putReplacesItemWithNewValue(String field, Object value) {
        Item before = createValid();
        ItemBuilder body = new ItemBuilder().name(before.name).description(before.description)
                .price(before.price).stock(before.stock);
        switch (field) {
            case "name" -> body.name(value);
            case "price" -> body.price(value);
            case "stock" -> body.stock(value);
            default -> body.description(value);
        }
        ApiResponse r = adminItems.update(before.id, body.build());
        Assert.assertEquals(r.status(), 200, r.toString());
        Assert.assertEquals(r.json().get(field).asText(), String.valueOf(value));
        Assert.assertEquals(r.json().get("id").asText(), before.id, "id is stable");

        // persisted
        Assert.assertEquals(adminItems.get(before.id).json().get(field).asText(), String.valueOf(value));
    }

    @Test
    public void putWithPartialBodyIsRejected_deviatesFromDocs() {
        Item i = createValid();
        ApiResponse r = adminItems.update(i.id, Map.of("price", 5));
        Assert.assertEquals(r.status(), 400, r.toString());
        Assert.assertTrue(r.errorMessage().contains("required"));
    }

    @Test
    public void updateWithInvalidDataIsRejected() {
        Item i = createValid();
        Assert.assertEquals(adminItems.update(i.id, new ItemBuilder().price(-1).build()).status(), 400);
        Assert.assertEquals(adminItems.update(i.id, new ItemBuilder().stock(-1).build()).status(), 400);
        Assert.assertEquals(adminItems.get(i.id).as(Item.class).price, i.price, 0.001, "unchanged");
    }

    @Test
    public void updateUnknownItemReturns404() {
        ApiResponse r = adminItems.update("999999999", new ItemBuilder().build());
        Assert.assertEquals(r.status(), 404, r.toString());
    }

    // ---------- DELETE ----------
    @Test
    public void deleteItemThenItIsGone() {
        Item i = createValid();
        Assert.assertEquals(adminItems.delete(i.id).status(), 204);
        Assert.assertEquals(adminItems.get(i.id).status(), 404);
        Assert.assertEquals(adminItems.delete(i.id).status(), 404, "second delete");
    }
}
