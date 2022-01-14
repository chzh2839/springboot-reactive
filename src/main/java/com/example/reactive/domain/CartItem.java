package com.example.reactive.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cartItem")
@Getter
public class CartItem {
    @Id private String cartItemId;
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
}
