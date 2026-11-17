package com.framework.api.builders;

import com.framework.utils.RandomDataUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DESIGN PATTERN: Builder.
 * Valid item by default; override or remove (omit) a field to build negative payloads.
 */
public class ItemBuilder {

    private final Map<String, Object> body = new LinkedHashMap<>();

    public ItemBuilder() {
        body.put("name", RandomDataUtil.alphaNumeric("Item"));
        body.put("description", "Automated test item");
        body.put("price", 99.99);
        body.put("stock", 10);
    }

    public ItemBuilder name(Object v) { body.put("name", v); return this; }
    public ItemBuilder description(Object v) { body.put("description", v); return this; }
    public ItemBuilder price(Object v) { body.put("price", v); return this; }
    public ItemBuilder stock(Object v) { body.put("stock", v); return this; }
    public ItemBuilder without(String field) { body.remove(field); return this; }

    public Map<String, Object> build() {
        return new LinkedHashMap<>(body);
    }
}
