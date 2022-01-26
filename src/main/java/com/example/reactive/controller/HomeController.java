package com.example.reactive.controller;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.Item;
import com.example.reactive.exception.DomainRuleViolationException;
import com.example.reactive.repository.CartRepository;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.service.CartService;
import com.example.reactive.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
    private Logger logger = LoggerFactory.getLogger(HomeController.class);

    private ItemRepository itemRepository;
    private CartRepository cartRepository;

    private CartService cartService;
    private ItemService itemService;

    //@Autowired 생략가능
    public HomeController(ItemRepository itemRepository, CartRepository cartRepository,
                          CartService cartService, ItemService itemService) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.itemService = itemService;
    }

//    @GetMapping
//    Mono<String> home() {
//        return Mono.just("home");
//    }

    @GetMapping
    Mono<Rendering> home() {
        return Mono.just(Rendering.view("home.html") // html 화면 렌더링
        .modelAttribute("items", this.itemRepository.findAll())
        .modelAttribute("cart", this.cartRepository.findById("My Cart").defaultIfEmpty(new Cart("My Cart")))
//        .modelAttribute("item", new Item())
        .build());
    }


    /** item **/

    @GetMapping(value = "/itemForm")
    Mono<Rendering> itemForm(@RequestParam(required = false) final String itemId) {
        Mono<Item> itemInfo = itemId != null ? this.itemRepository.findById(itemId) : Mono.just(new Item());
        return Mono.just(Rendering.view("itemForm.html") // html 화면 렌더링
                .modelAttribute("item", itemInfo)
                .build());
    }

    @GetMapping(value = "/detail/{itemId}")
    Mono<Rendering> getDetail(@PathVariable final String itemId) {
        return Mono.just(Rendering.view("detail.html")
                .modelAttribute("detail", this.itemRepository.findById(itemId))
                .build());
    }

    /*
    @GetMapping(value = "/search")
    Mono<Rendering> search(@RequestParam(required = false) String name,
                           @RequestParam(required = false) String description,
                           @RequestParam boolean useAnd) {
        logger.info("search {} {} {}", name, description, useAnd);
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items", this.itemService.searchByExample(name, description, useAnd))
                .modelAttribute("cart", this.cartRepository.findById("My Cart").defaultIfEmpty(new Cart("My Cart")))
                .build());
    }*/

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
        Mono<Void> result = this.itemService.deleteItem(itemId);
        return result == null ? result.onErrorResume(e -> Mono.error(new DomainRuleViolationException("username is required"))).thenReturn("redirect:/") : result.thenReturn("redirect:/");
    }


    /** cart **/

    @PostMapping(value = "/addToCart/{itemId}")
    Mono<String> addToCart(@PathVariable final String itemId) {
        logger.info("addToCart {}", itemId);
        return this.cartService.addToCart("My Cart", itemId)
                .thenReturn("redirect:/");
    }

    @DeleteMapping(value = "/deleteCartItem/{itemId}")
    Mono<String> deleteCartItem(@PathVariable final String itemId, final Model model) {
        logger.info("deleteCartItem {}", itemId);
        model.addAttribute("cart", this.cartService.deleteCartItem("My Cart", itemId));
        return Mono.just("redirect:/");
    }
}
