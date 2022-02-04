package com.example.reactive.service;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.CartItem;
import com.example.reactive.exception.DomainRuleViolationException;
import com.example.reactive.repository.CartRepository;
import com.example.reactive.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CartService {
    private Logger logger = LoggerFactory.getLogger(CartService.class);

    private ItemRepository itemRepository;
    private CartRepository cartRepository;

    public CartService(ItemRepository itemRepository, CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    public Mono<Cart> getCart(String cartId) {
        return this.cartRepository.findById(cartId);
    }

    public Mono<Cart> addToCart(String cartId, String itemId) {
        logger.info("addToCart {} {}", cartId, itemId);
        return this.cartRepository.findById(cartId)
                .log("foundCart")
                .defaultIfEmpty(new Cart(cartId))
                .log("emptyCart")
                .flatMap(cart -> cart.getCartItems().stream().filter(cartItem -> itemId.equals(cartItem.getItem().getItemId())).findAny()
                    .map(cartItem -> { // 카트에 담겨있던 상품이라면 수량 +1
                        cartItem.increment();
                        return Mono.just(cart).log("newCartItem");
                    })
                    .orElseGet(()-> this.itemRepository.findById(itemId).log("fetchedItem") // 카트에 담겨 있지 않은 상품이라면 새 row 추가
                                .map(CartItem::new).log("cartItem") // item -> new CartItem(item)
                                .doOnNext(cartItem -> cart.getCartItems().add(cartItem))
                                .map(cartItem -> cart)).log("addedCartItem")
                )
                .log("cartWithAnotherItem")
                .flatMap(this.cartRepository::save) // 업데이트된 카트를 몽고디비에 저장
                .log("savedCart");
    }

    public Mono<Void> deleteMyCart(String cartId){
        logger.info("deleteMyCart {}", cartId);
        return this.cartRepository.deleteById(cartId);
    }

    public Mono<Cart> deleteCartItem(String cartId, String itemId) {
        logger.info("deleteCartItem {} {}", cartId, itemId);
        return this.cartRepository.findById(cartId)
                .flatMap(cart -> cart.getCartItems().stream().filter(cartItem -> itemId.equals(cartItem.getItem().getItemId())).findAny()
                        .map(cartItem -> {
                            cart.getCartItems().remove(cartItem);
                            return this.cartRepository.save(cart);
                        }).orElseGet(()->{
                            throw new DomainRuleViolationException("존재하지 않는 상품입니다.");
                        }));
    }
}
