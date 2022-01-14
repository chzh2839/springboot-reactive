package com.example.reactive.service;

/* This is just for server test.
 *  No using on the other */

import com.example.reactive.domain.Dish;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class KitchenService {
    public Flux<Dish> getDishes() {
        return Flux.just(
                new Dish("dish1"),
                new Dish("dish2"),
                new Dish("dish3")
        );
    }
}
