package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/item")
public class ItemController {
    private Logger logger = LoggerFactory.getLogger(ItemController.class);

    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<Flux<Item>> getItems() {
        return ResponseEntity.ok(this.itemService.getItems());
    }

    @GetMapping(value = "/{itemId}")
    public ResponseEntity<Mono<Item>> getItem(@PathVariable final String itemId) {
        return ResponseEntity.ok(this.itemService.getItem(itemId));
    }

    @GetMapping("/price-range")
    public ResponseEntity<Flux<Item>> getItem(@RequestParam("min") double min, @RequestParam("max") double max) {
        logger.info("getItem price-range {} {}", min, max);
        return ResponseEntity.ok(this.itemService.getItemPriceInRange(min, max));
    }

    @PostMapping()
    ResponseEntity<Mono<Item>> saveItem(@RequestBody Item item) {
        logger.info("saveItem {}", item.toString());
        return ResponseEntity.ok(this.itemService.insertItem(item));
    }

    @PutMapping(value = "/{itemId}")
    ResponseEntity updateItem(@RequestBody Item item, @PathVariable("itemId") final String itemId) {
        logger.info("updateItem {} {}", item.toString(), itemId);
        return ResponseEntity.ok(this.itemService.updateItem(item, itemId));
    }

    @DeleteMapping(value = "/{itemId}")
    ResponseEntity<Mono<Void>> deleteItem(@PathVariable("itemId") final String itemId) {
        logger.info("deleteItem {}", itemId);
        return ResponseEntity.ok(this.itemService.deleteItem(itemId));
    }


}
