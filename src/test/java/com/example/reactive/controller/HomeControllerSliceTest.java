package com.example.reactive.controller;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.Item;
import com.example.reactive.repository.CartRepository;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.service.CartService;
import com.example.reactive.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebFluxTest(HomeController.class)
public class HomeControllerSliceTest {
    @Autowired
    private WebTestClient client;

    @MockBean ItemService itemService;
    @MockBean CartService cartService;

    @Test
    void homePage() {
        when(itemService.getItems()).thenReturn(Flux.just(
                new Item("apple","사과", 10.05),
                new Item("lemon","레몬", 7.34)
        ));
        when(cartService.getCart("My Cart"))
                .thenReturn(Mono.just(new Cart("My Cart")));

        client.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    assertThat(stringEntityExchangeResult.getResponseBody()).contains("action=\"/addToCart/");
                });
    }
}
