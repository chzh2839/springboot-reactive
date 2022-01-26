package com.example.reactive.repository;

import com.example.reactive.domain.CartItem;
import com.example.reactive.domain.Item;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, String> {
    @Query("{'item.itemId' : itemId}")
    Flux<CartItem> findAllByItemId(String itemId);

    @Query("{'item.itemId' : itemId}")
    Mono<Void> deleteCartItemByItemId(String itemId);
}
