package com.framework.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    public String id;
    public String customerId;
    public List<OrderLine> items;
    public double total;
    public String status;
    public long createdAt;
    public boolean paid;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderLine {
        public String itemId;
        public String name;
        public double price;
        public int quantity;
    }
}
