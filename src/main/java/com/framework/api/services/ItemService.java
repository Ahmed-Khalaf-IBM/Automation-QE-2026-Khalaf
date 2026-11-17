package com.framework.api.services;

import com.framework.api.client.ApiClient;
import com.framework.api.client.ApiResponse;

public class ItemService {
    private final ApiClient client;

    public ItemService(ApiClient client) {
        this.client = client;
    }

    public ApiResponse list() { return client.get("/items"); }
    public ApiResponse get(String id) { return client.get("/items/" + id); }
    public ApiResponse create(Object body) { return client.post("/items", body); }
    public ApiResponse update(String id, Object body) { return client.put("/items/" + id, body); }
    public ApiResponse delete(String id) { return client.delete("/items/" + id); }
}
