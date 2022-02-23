package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.service.ItemService;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mediatype.alps.Alps.*;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;

@RestController
@RequestMapping("/hypermedia")
public class HypermediaItemController {
    private final ItemService itemService;
    private final ItemRepository itemRepository;

    public HypermediaItemController(ItemService itemService, ItemRepository itemRepository) {
        this.itemService = itemService;
        this.itemRepository = itemRepository;
    }

    @GetMapping
    Mono<RepresentationModel<?>> root() {
        HypermediaItemController controller = methodOn(HypermediaItemController.class);
        Mono<Link> selfLink = linkTo(controller.root()).withSelfRel().toMono();
        Mono<Link> itemsAggregateLink = linkTo(controller.getItems()).withRel(IanaLinkRelations.ITEM).toMono();
        return selfLink.zipWith(itemsAggregateLink)
                .map(links -> Links.of(links.getT1(), links.getT2()))
                .map(links -> new RepresentationModel<>(links.toList()));
    }

    @GetMapping("/items")
    Mono<CollectionModel<EntityModel<Item>>> getItems() {
        return this.itemService.getItems()
                .flatMap(item -> getItem(item.getItemId()))
                .collectList()
                .flatMap(entityModels -> linkTo(methodOn(HypermediaItemController.class)
                                    .getItems()).withSelfRel().toMono()
                                    .map(selfLink -> CollectionModel.of(entityModels, selfLink)));
    }

    @GetMapping("/items/{itemId}")
    Mono<EntityModel<Item>> getItem(@PathVariable String itemId) {
        /** 1. 스프링 헤이티오스의 정적메서드 WebFluxLinkBuilder.methodOn()로 컨트롤러에 대한 프록시를 생성 */
        HypermediaItemController controller = methodOn(HypermediaItemController.class);

        /** 2. WebFluxLinkBuilder.linkTo()로 getItem()메서드에 대한 링크를 생성.
         *      현재 메서드가 getItem()이므로, self라는 이름의 링크를 추가하고 리액터 Mono에 담아 반환. */
        Mono<Link> selfLink = linkTo(controller.getItem(itemId)).withSelfRel().toMono();

        /** 3. 모든 상품을 반환하는 getItems()를 찾아 애그리것 루트에 대한 링크를 생성.
         * (=> 애그리것 루트(aggregate root) : 단순히 어떤 엔티티의 목록을 볼 수 있는 링크 <=)
         * IANA(Internet Assigned Numbers Authority, 인터넷 할당 번호 관리기관) 표준에 따라 링크 이름을 item으로 명명.
         *  */
        Mono<Link> aggregateLink = linkTo(controller.getItems()).withRel(IanaLinkRelations.ITEM).toMono();

        /** 4. Mono.zip()으로 여러 개의 비동기 요청을 실행하고, 결과를 하나로 합침.
         *      아래에서는 getItem(), selfLink, aggregateLink 결과를 타입 안전성이 보장되는 리액터 Tuple타입에 넣고 Mono로 감싸서 반환
         *  5. map()을 통해 Tuple에 담긴 여러 비동기 요청 결과를 꺼내서 EntityModel을 만들고 Mono로 감싸서 반환. */
        return Mono.zip(itemService.getItem(itemId), selfLink, aggregateLink)
                .map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3())));
    }

    @PostMapping("/items")
    Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<EntityModel<Item>> item) {
        return item.map(EntityModel::getContent)
                .flatMap(this.itemRepository::save)
                .map(Item::getItemId)
                .flatMap(this::getItem)
                .map(newModel -> ResponseEntity.created(newModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).build());
    }

    @PutMapping("/items/{itemId}")
    Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<EntityModel<Item>> item, @PathVariable String itemId) {
        return item.map(EntityModel::getContent)
                .map(content -> new Item(itemId, content.getName(), content.getDescription(), content.getPrice()))
                .flatMap(this.itemRepository::save)
                .then(getItem(itemId))
                .map(model -> ResponseEntity.noContent()
                            .location(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).build());
    }

    // 하이퍼미디어는 단순히 데이터만이 아니라 데이터 사용방법에 대한 정보도 함께 제공.
    @GetMapping(value = "/items/profile", produces = MediaTypes.ALPS_JSON_VALUE)
    public Alps profile() {
        return alps().descriptor(Collections.singletonList(descriptor()
                                .id(Item.class.getSimpleName() + "-repr")
                                .descriptor(Arrays.stream(Item.class.getDeclaredFields())
                                .map(field -> descriptor().name(field.getName()).type(Type.SEMANTIC).build())
                                .collect(Collectors.toList()))
                                .build()))
                .build();
    }


    /** 행동유도성(affordances)이 있는 API - getItems */
    @GetMapping("/items/affordances")
    Mono<CollectionModel<EntityModel<Item>>> getItemsWithAffordances() {
        HypermediaItemController controller = methodOn(HypermediaItemController.class);

        Mono<Link> aggregateRoot = linkTo(controller.getItems())
                                    .withSelfRel()
                                    .andAffordance(controller.addNewItem(null)).toMono();

        return this.itemService.getItems()
                .flatMap(item -> getItem(item.getItemId()))
                .collectList()
                .flatMap(models -> aggregateRoot.map(selfLink -> CollectionModel.of(models, selfLink)));
    }

    /** 행동유도성(affordances)이 있는 API - getItem */
    @GetMapping("/items/{itemId}/affordances")
    Mono<EntityModel<Item>> getItemWithAffordances(@PathVariable String itemId) {
        HypermediaItemController controller = methodOn(HypermediaItemController.class);

        Mono<Link> selfLink = linkTo(controller.getItem(itemId)).withSelfRel()
                .andAffordance(controller.updateItem(null, itemId))
                // andAffordance()는 Item을 수정할 수 있는 updateItem()메서드에 사용되는 경로를 getItem()메서드의 self 링크에 연결한다.
                .toMono();

        Mono<Link> aggregateLink = linkTo(controller.getItems()).withRel(IanaLinkRelations.ITEM).toMono();

        return Mono.zip(itemService.getItem(itemId), selfLink, aggregateLink)
                .map(o -> EntityModel.of(o.getT1(), o.getT2(), o.getT3()));
    }



}
