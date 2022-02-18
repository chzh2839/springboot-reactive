package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.service.ItemService;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.mediatype.alps.Alps.*;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;

@RestController
@RequestMapping("/hypermedia")
public class HypermediaItemController {
    private final ItemService itemService;

    public HypermediaItemController(ItemService itemService) {
        this.itemService = itemService;
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



}
