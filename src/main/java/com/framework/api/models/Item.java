package com.framework.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    public String id;
    public String name;
    public String description;
    public double price;
    public int stock;
}
