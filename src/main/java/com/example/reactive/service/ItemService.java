package com.example.reactive.service;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.CartItemRepository;
import com.example.reactive.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ItemService {
    private Logger logger = LoggerFactory.getLogger(ItemService.class);

    private ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Flux<Item> getItems() {
        return this.itemRepository.findAll();
    }

    public Mono<Item> getItem(String itemId) {
        return this.itemRepository.findById(itemId);
    }

    public Flux<Item> getItemPriceInRange(double min, double max) {
        return this.itemRepository.findByPriceBetween(Range.closed(min, max));
    }


    public Flux<Item> searchByExample(String name, String description, boolean useAnd) {
        logger.info("searchByExample {} {} {}", name, description, useAnd);

        // Probe는 필드에 어떤 값들을 가지고 있는 도메인 객체
        // ExampleMatcher는 Probe에 들어있는 그 필드의 값들을 어떻게 쿼리할 데이터와 비교할지 정의한 것
        // Example은 그 둘을 하나로 합친 것 이걸로 쿼리를 함

        Item item = new Item(name, description, 0.0); // probe

        ExampleMatcher matcher = (useAnd ? ExampleMatcher.matchingAll() : ExampleMatcher.matchingAny()) // useAnd 값에 따라 ExampleMatcher 분기처리
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) // 부분 일치 검색
                .withIgnoreCase() // 대소문자 구분 X
                .withIncludeNullValues()
                .withIgnorePaths("price"); // ExampleMatcher은 기본적으로 null필드를 무시하지만, 기본타입인 double에는 null이 올 수 없으므로 price 필드가 무시되도록 지정

        Example<Item> probe = Example.of(item, matcher);
        return itemRepository.findAll(probe);
    }

    public Mono<Item> insertItem(Item item) {
        return this.itemRepository.save(item);
    }

    public Mono<Item> updateItem(Item item, String itemId) {
        return this.itemRepository.findById(itemId).flatMap(obj -> {
            obj.setItemId(itemId);
            obj.setName(item.getName());
            obj.setPrice(item.getPrice());
            obj.setDescription(item.getDescription());
            obj.setReleaseDate(item.getReleaseDate());
            obj.setAvailableUnits(item.getAvailableUnits());
            obj.setActive(item.getActive());
            return Mono.just(obj);
        }).flatMap(itemRepository::save);
    }

    public Mono<Void> deleteItem(String itemId) {
        return this.itemRepository.deleteById(itemId);
    }
}
