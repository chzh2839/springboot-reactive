package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.example.reactive.SecurityConfig.*;
import static org.springframework.hateoas.mediatype.alps.Alps.*;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;

@RestController
@RequestMapping("/api/items")
public class ApiItemController {
    private static final SimpleGrantedAuthority ROLE_INVENTORY = new SimpleGrantedAuthority("ROLE_" + INVENTORY);

    private final ItemRepository repository;

    public ApiItemController(ItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    Mono<RepresentationModel<?>> root() {
        ApiItemController controller = methodOn(ApiItemController.class);

        Mono<Link> selfLink = linkTo(controller.root()).withSelfRel() //
                .toMono();

        Mono<Link> itemsAggregateLink = linkTo(controller.findAll(null)) //
                .withRel(IanaLinkRelations.ITEM) //
                .toMono();

        return Mono.zip(selfLink, itemsAggregateLink) //
                .map(links -> Links.of(links.getT1(), links.getT2())) //
                .map(links -> new RepresentationModel<>(links.toList()));
    }

    @GetMapping("/all")
    Mono<CollectionModel<EntityModel<Item>>> findAll(Authentication auth) {
        ApiItemController controller = methodOn(ApiItemController.class);

        Mono<Link> selfLink = linkTo(controller.findAll(auth)).withSelfRel().toMono();

        Mono<Links> allLinks;

        if (auth.getAuthorities().contains(ROLE_INVENTORY)) {
            Mono<Link> addNewLink = linkTo(controller.addNewItem(null, auth)).withRel("add").toMono();

            allLinks = Mono.zip(selfLink, addNewLink)
                    .map(links -> Links.of(links.getT1(), links.getT2()));
        } else {
            allLinks = selfLink
                    .map(link -> Links.of(link));
        }

        return allLinks //
                .flatMap(links -> this.repository.findAll()
                        .flatMap(item -> findOne(item.getItemId(), auth))
                        .collectList() //
                        .map(entityModels -> CollectionModel.of(entityModels, links)));
    }

    @GetMapping("/{itemId}")
    Mono<EntityModel<Item>> findOne(@PathVariable String itemId, Authentication auth) {
        ApiItemController controller = methodOn(ApiItemController.class);

        Mono<Link> selfLink = linkTo(controller.findOne(itemId, auth)).withSelfRel()
                .toMono();

        Mono<Link> aggregateLink = linkTo(controller.findAll(auth))
                .withRel(IanaLinkRelations.ITEM).toMono();

        Mono<Links> allLinks;

        if (auth.getAuthorities().contains(ROLE_INVENTORY)) {
            Mono<Link> deleteLink = linkTo(controller.deleteItem(itemId)).withRel("delete")
                    .toMono();
            allLinks = Mono.zip(selfLink, aggregateLink, deleteLink)
                    .map(links -> Links.of(links.getT1(), links.getT2(), links.getT3()));
        } else {
            allLinks = Mono.zip(selfLink, aggregateLink)
                    .map(links -> Links.of(links.getT1(), links.getT2()));
        }

        return this.repository.findById(itemId)
                .zipWith(allLinks)
                .map(o -> EntityModel.of(o.getT1(), o.getT2()));
    }

    @PreAuthorize("hasRole('" + INVENTORY + "')")
    @PostMapping("/add")
    Mono<ResponseEntity<?>> addNewItem(@RequestBody Item item, Authentication auth) {
        return this.repository.save(item)
                .map(Item::getItemId) //
                .flatMap(id -> findOne(id, auth))
                .map(newModel -> ResponseEntity.created(newModel
                        .getRequiredLink(IanaLinkRelations.SELF)
                        .toUri()).build());
    }

    @PreAuthorize("hasRole('" + INVENTORY + "')")
    @DeleteMapping("/delete/{itemId}")
    Mono<ResponseEntity<?>> deleteItem(@PathVariable String itemId) {
        return this.repository.deleteById(itemId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
