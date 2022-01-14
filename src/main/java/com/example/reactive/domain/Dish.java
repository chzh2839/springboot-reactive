package com.example.reactive.domain;

/* This is just for server test.
 *  No using on the other */

import lombok.Getter;

@Getter
public class Dish {
    private String dishName;

    public Dish() {
    }

    public Dish(String dishName) {
        this.dishName = dishName;
    }
}
