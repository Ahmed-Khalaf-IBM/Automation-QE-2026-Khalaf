package com.framework.api.builders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** DESIGN PATTERN: Builder - for order payloads. */
public class OrderBuilder {

    private Object customerId = "user1";
    private final List<Map<String, Object>> items = new ArrayList<>();

    public OrderBuilder customerId(Object v) { this.customerId = v; return this; }

    public OrderBuilder addItem(String itemId, Object quantity) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("itemId", itemId);
        line.put("quantity", quantity);
        items.add(line);
        return this;
    }

    public Map<String, Object> build() {
        Map<String, Object> body = new LinkedHashMap<>();
        if (customerId != null) body.put("customerId", customerId);
        body.put("items", new ArrayList<>(items));
        return body;
    }
}
