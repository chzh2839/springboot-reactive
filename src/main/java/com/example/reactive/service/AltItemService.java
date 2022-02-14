package com.example.reactive.service;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.CartItem;
import com.example.reactive.domain.Item;
import com.example.reactive.repository.CartRepository;
import com.example.reactive.repository.ItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class AltItemService {
    private ItemRepository itemRepository;
    private CartRepository cartRepository;

    public AltItemService(ItemRepository itemRepository, CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    public Mono<Cart> getCart(String cartId) {
        return this.cartRepository.findById(cartId);
    }

    public Flux<Item> getItems() {
        return this.itemRepository.findAll();
    }

    public Mono<Item> saveItem(Item newItem) {
        return this.itemRepository.save(newItem);
    }

    public Mono<Void> deleteItem(String itemId) {
        return this.itemRepository.deleteById(itemId);
    }

    public Mono<Cart> addItemToCart(String cartId, String itemId) {
        Cart myCart = this.cartRepository.findById(cartId)
                .defaultIfEmpty(new Cart(cartId))
                .block();

        return myCart.getCartItems().stream()
                .filter(cartItem -> cartItem.getItem().getItemId().equals(itemId))
                .findAny()
                .map(cartItem -> {
                    cartItem.increment();
                    return Mono.just(myCart);
                })
                .orElseGet(() -> this.itemRepository.findById(itemId)
                                .map(item -> new CartItem(item))
                                .map(cartItem -> {
                                    myCart.getCartItems().add(cartItem);
                                    return myCart;
                                }))
                .flatMap(cart -> this.cartRepository.save(cart));
    }

    public Mono<Cart> removeOneFromCart(String cartId, String itemId) {
        return this.cartRepository.findById(cartId)
                .defaultIfEmpty(new Cart(cartId))
                .flatMap(cart -> cart.getCartItems().stream()
                            .filter(cartItem -> cartItem.getItem().getItemId().equals(itemId))
                            .findAny()
                            .map(cartItem -> {
                                cartItem.decrement();
                                return Mono.just(cart);
                            })
                            .orElse(Mono.empty()))
                .map(cart -> new Cart(cart.getCartId(), cart.getCartItems().stream()
                                .filter(cartItem -> cartItem.getQuantity() > 0)
                                .collect(Collectors.toList())))
                .flatMap(cart -> this.cartRepository.save(cart));
    }
}
