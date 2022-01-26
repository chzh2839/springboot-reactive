package com.example.reactive.domain;

import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cartItem")
@Getter
public class CartItem {
    private Item item;
    private int quantity;

    public CartItem() {
    }

    public CartItem(Item item) {
        this();
        this.item = item;
        this.quantity = 1;
    }

    public CartItem(Item item, int quantity) {
        this();
        this.item = item;
        this.quantity = quantity;
    }

    public void increment() {
        this.quantity++;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                ", item=" + item +
                ", quantity=" + quantity +
                '}';
    }
}
