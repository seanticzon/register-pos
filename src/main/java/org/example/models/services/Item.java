package org.example.models.services;

import java.math.BigDecimal;

public class Item {
    public String id;
    public String name;
    public BigDecimal price;

    public Item(String id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
