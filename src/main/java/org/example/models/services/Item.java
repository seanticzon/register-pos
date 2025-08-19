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

    public String getId() {
        return id;
    }

    public String getName() {  // add this getter
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
