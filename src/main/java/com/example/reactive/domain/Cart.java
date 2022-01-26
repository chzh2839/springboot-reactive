package com.example.reactive.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "cart")
@Getter
public class Cart {
    @Id
    private String cartId;
    private List<CartItem> cartItems;

    Cart() {
    }

    public Cart(String cartId) {
        this(cartId, new ArrayList<>());
    }

    public Cart(String cartId, List<CartItem> cartItems) {
        this.cartId = cartId;
        this.cartItems = cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "cartId='" + cartId + '\'' +
                ", cartItems=" + cartItems +
                '}';
    }
}
