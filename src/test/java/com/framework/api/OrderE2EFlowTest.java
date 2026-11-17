package com.framework.api;

import com.framework.api.builders.ItemBuilder;
import com.framework.api.builders.OrderBuilder;
import com.framework.api.client.ApiClient;
import com.framework.api.client.ApiClientFactory;
import com.framework.api.client.ApiClientFactory.Role;
import com.framework.api.client.ApiResponse;
import com.framework.api.models.Item;
import com.framework.api.models.Order;
import com.framework.api.services.ItemService;
import com.framework.api.services.OrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * E2E business flow from docs/API_FLOWS.md  ("Creating and Checking Out an Order"):
 * admin creates item -> customer browses -> admin creates order for customer (stock drops)
 * -> customer sees own order -> customer checks out -> admin sees it as paid
 * -> double checkout rejected -> admin deletes order (stock restored) -> admin deletes item.
 * Steps share state, so they run in a fixed order and depend on each other.
 */
public class OrderE2EFlowTest {

    private static final int INITIAL_STOCK = 10;
    private static final int QTY = 3;

    private ApiClient admin;
    private ApiClient customer;
    private ItemService adminItems;
    private ItemService customerItems;
    private OrderService adminOrders;
    private OrderService customerOrders;

    private Item item;
    private Order order;

    @BeforeClass
    public void setUp() {
        admin = ApiClientFactory.create(Role.ADMIN);
        customer = ApiClientFactory.create(Role.CUSTOMER);
        adminItems = new ItemService(admin);
        customerItems = new ItemService(customer);
        adminOrders = new OrderService(admin);
        customerOrders = new OrderService(customer);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        // best-effort so a failed run does not leave data behind
        if (order != null) adminOrders.delete(order.id);
        if (item != null) adminItems.delete(item.id);
        admin.close();
        customer.close();
    }

    @Test(priority = 1)
    public void step1_adminCreatesItem() {
        ApiResponse r = adminItems.create(new ItemBuilder().price(150.00).stock(INITIAL_STOCK).build());
        Assert.assertEquals(r.status(), 201, r.toString());
        item = r.as(Item.class);
        Assert.assertNotNull(item.id);
        Assert.assertEquals(item.stock, INITIAL_STOCK);
    }

    @Test(priority = 2, dependsOnMethods = "step1_adminCreatesItem")
    public void step2_customerSeesItemInCatalogue() {
        ApiResponse r = customerItems.list();
        Assert.assertEquals(r.status(), 200);
        List<Item> all = r.as(new TypeReference<List<Item>>() {});
        Assert.assertTrue(all.stream().anyMatch(i -> i.id.equals(item.id)), "new item must be listed");
    }

    @Test(priority = 3, dependsOnMethods = "step1_adminCreatesItem")
    public void step3_adminCreatesOrderForCustomer() {
        ApiResponse r = adminOrders.create(new OrderBuilder().customerId("user1").addItem(item.id, QTY).build());
        Assert.assertEquals(r.status(), 201, r.toString());
        order = r.as(Order.class);
        Assert.assertEquals(order.status, "pending");
        Assert.assertFalse(order.paid);
        Assert.assertEquals(order.total, 150.00 * QTY, 0.001, "total = price x quantity");
        Assert.assertEquals(order.items.get(0).quantity, QTY);
    }

    @Test(priority = 4, dependsOnMethods = "step3_adminCreatesOrderForCustomer")
    public void step4_stockWasDecremented() {
        Item current = adminItems.get(item.id).as(Item.class);
        Assert.assertEquals(current.stock, INITIAL_STOCK - QTY);
    }

    @Test(priority = 5, dependsOnMethods = "step3_adminCreatesOrderForCustomer")
    public void step5_customerSeesOnlyOwnOrder() {
        ApiResponse r = customerOrders.list();
        Assert.assertEquals(r.status(), 200);
        List<Order> mine = r.as(new TypeReference<List<Order>>() {});
        Assert.assertTrue(mine.stream().anyMatch(o -> o.id.equals(order.id)), "own order is listed");
        Assert.assertTrue(mine.stream().allMatch(o -> o.customerId.equals("user1")), "no foreign orders");
    }

    @Test(priority = 6, dependsOnMethods = "step3_adminCreatesOrderForCustomer")
    public void step6_customerChecksOut() {
        ApiResponse r = customerOrders.checkout(order.id);
        Assert.assertEquals(r.status(), 200, r.toString());
        Order paid = r.as(Order.class);
        Assert.assertEquals(paid.status, "paid");
        Assert.assertTrue(paid.paid);
    }

    @Test(priority = 7, dependsOnMethods = "step6_customerChecksOut")
    public void step7_adminSeesOrderInPaidList() {
        ApiResponse r = adminOrders.listPaid();
        Assert.assertEquals(r.status(), 200);
        List<Order> paid = r.as(new TypeReference<List<Order>>() {});
        Assert.assertTrue(paid.stream().anyMatch(o -> o.id.equals(order.id)));
    }

    @Test(priority = 8, dependsOnMethods = "step6_customerChecksOut")
    public void step8_secondCheckoutIsRejected() {
        ApiResponse r = customerOrders.checkout(order.id);
        Assert.assertEquals(r.status(), 400, r.toString());
        Assert.assertTrue(r.errorMessage().contains("already been paid"));
    }

    @Test(priority = 9, dependsOnMethods = "step6_customerChecksOut")
    public void step9_adminDeletesOrderAndStockIsRestored() {
        ApiResponse del = adminOrders.delete(order.id);
        Assert.assertEquals(del.status(), 204, del.toString());
        order = null; // already gone, skip in cleanup
        Assert.assertEquals(adminItems.get(item.id).as(Item.class).stock, INITIAL_STOCK);
    }

    @Test(priority = 10, dependsOnMethods = "step9_adminDeletesOrderAndStockIsRestored")
    public void step10_adminDeletesItem() {
        Assert.assertEquals(adminItems.delete(item.id).status(), 204);
        Assert.assertEquals(adminItems.get(item.id).status(), 404, "deleted item must be gone");
        item = null;
    }
}
