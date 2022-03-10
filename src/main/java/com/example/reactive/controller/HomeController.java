package com.example.reactive.controller;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.Item;
import com.example.reactive.exception.DomainRuleViolationException;
import com.example.reactive.service.CartService;
import com.example.reactive.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
    private Logger logger = LoggerFactory.getLogger(HomeController.class);

    private CartService cartService;
    private ItemService itemService;

    //@Autowired 생략가능
    public HomeController(CartService cartService, ItemService itemService) {
        this.cartService = cartService;
        this.itemService = itemService;
    }

    private static String cartName(Authentication auth) {
        return auth.getName() + "'s Cart";
    }

//    @GetMapping
//    Mono<String> home() {
//        return Mono.just("home");
//    }

    @GetMapping
    Mono<Rendering> home(Authentication auth) {
        return Mono.just(Rendering.view("home.html") // html 화면 렌더링
        .modelAttribute("items", this.itemService.getItems()) // .doOnNext(System.out::println)
        .modelAttribute("cart", this.cartService.getCart(cartName(auth)).defaultIfEmpty(new Cart(cartName(auth))))
        .modelAttribute("auth", auth)
        .build());
    }

    @GetMapping(value = "/search")
    Mono<Rendering> search(@RequestParam(required = false) String name,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) String useAnd) {
        logger.info("search {} {} {}", name, description, useAnd);
        boolean useAndVal = useAnd != null ? Boolean.valueOf(useAnd) : false;
        return Mono.just(Rendering.view("home.html")
        .modelAttribute("items", this.itemService.searchByExample(name, description, useAndVal).doOnNext(System.out::println))
        .modelAttribute("cart", this.cartService.getCart("My Cart").defaultIfEmpty(new Cart("My Cart")))
        .build());
    }


    /** item **/

    @GetMapping(value = "/itemForm")
    Mono<Rendering> itemForm(@RequestParam(required = false) final String itemId) {
        Mono<Item> itemInfo = itemId != null ? this.itemService.getItem(itemId) : Mono.just(new Item());
        return Mono.just(Rendering.view("itemForm.html") // html 화면 렌더링
                .modelAttribute("item", itemInfo)
                .build());
    }

    @GetMapping(value = "/detail/{itemId}")
    Mono<Rendering> getDetail(@PathVariable final String itemId) {
        return Mono.just(Rendering.view("detail.html")
                .modelAttribute("detail", this.itemService.getItem(itemId))
                .build());
    }

    @PostMapping(value = "/saveItem")
    Mono<String> insertItem(Item item) {
        logger.info("insertItem {}", item.toString());
        return this.itemService.insertItem(item).thenReturn("redirect:/");
    }

    @PostMapping(value = "/saveItem/{itemId}")
    Mono<String> updateItem(Item item, @PathVariable final String itemId) {
        logger.info("updateItem {}", item.toString());
        return this.itemService.updateItem(item, itemId).thenReturn("redirect:/detail/"+itemId);
    }

    @DeleteMapping(value = "/deleteItem/{itemId}")
    Mono<String> deleteItemIgnoreCart(@PathVariable final String itemId) {
        logger.info("deleteItem {}", itemId);
        if(itemId == null || itemId.isEmpty()) {
            new DomainRuleViolationException("itemId is required");
        }
        return this.itemService.deleteItem(itemId).thenReturn("redirect:/");
    }


    /** cart **/

    @PostMapping(value = "/addToCart/{itemId}")
    Mono<String> addToCart(@PathVariable final String itemId, Authentication auth) {
        logger.info("addToCart {}", itemId);
        return this.cartService.addToCart(cartName(auth), itemId)
                .thenReturn("redirect:/");
    }

    @DeleteMapping(value = "/deleteCartItem/{itemId}")
    Mono<String> deleteCartItem(@PathVariable final String itemId, final Model model, Authentication auth) {
        logger.info("deleteCartItem {}", itemId);
        model.addAttribute("cart", this.cartService.deleteCartItem(cartName(auth), itemId));
        return Mono.just("redirect:/");
    }
}
