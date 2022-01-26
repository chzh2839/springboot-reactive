package com.example.reactive.controller;

import com.example.reactive.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/cart")
public class CartController {
    private Logger logger = LoggerFactory.getLogger(CartController.class);

    private CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @DeleteMapping(value = "/deleteMyCart")
    ResponseEntity<Mono<Void>> deleteMyCart() {
        logger.info("deleteMyCart");
        return ResponseEntity.ok(this.cartService.deleteMyCart("My Cart"));
    }

//    @DeleteMapping(value = "/deleteCartItem/{itemId}")
//    ResponseEntity<Mono<Cart>> deleteCartItem(@PathVariable final String itemId) {
//        logger.info("deleteCartItem {}", itemId);
//        return ResponseEntity.ok(this.cartService.deleteCartItem("My Cart", itemId));
//    }

}
