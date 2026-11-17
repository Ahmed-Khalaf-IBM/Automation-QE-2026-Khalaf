package com.framework.api.services;

import com.framework.api.client.ApiClient;
import com.framework.api.client.ApiResponse;

public class OrderService {
    private final ApiClient client;

    public OrderService(ApiClient client) {
        this.client = client;
    }

    public ApiResponse create(Object body) { return client.post("/orders", body); }
    public ApiResponse list() { return client.get("/orders"); }
    public ApiResponse listPaid() { return client.get("/orders/paid"); }
    public ApiResponse checkout(String id) { return client.post("/orders/" + id + "/checkout", null); }
    public ApiResponse delete(String id) { return client.delete("/orders/" + id); }
}
