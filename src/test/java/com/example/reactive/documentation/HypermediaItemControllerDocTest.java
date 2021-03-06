package com.example.reactive.documentation;

import com.example.reactive.controller.HypermediaItemController;
import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.*;

@WebFluxTest(controllers = HypermediaItemController.class)
@AutoConfigureRestDocs
public class HypermediaItemControllerDocTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    ItemService itemService;

    @MockBean
    ItemRepository itemRepository;

    @Test
    void getAllItems() {
        when(itemService.getItems()).thenReturn(Flux.just(new Item("purple bottle", "보라색 물병", 23.22)));
        when(itemService.getItem((String) null)).thenReturn(Mono.just(new Item("item-1", "purple lipstick", "보라색 립스틱", 34.66)));

        this.webTestClient.get().uri("/hypermedia/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("getItems-hypermedia", preprocessResponse(prettyPrint())));
    }

    @Test
    void getOneItem() {
        when(itemService.getItem("item-1")).thenReturn(Mono.just(new Item("item-1", "gold candy bar", "골드 캔디바", 70.12)));

        this.webTestClient.get().uri("/hypermedia/items/item-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("getItem-hypermedia", preprocessResponse(prettyPrint()),
                                    links( // 응답에 링크가 포함된 문서 조각 만들기
                                        linkWithRel("self").description("이 `item`에 대한 공식 링크"), // Item객체  자신을 나타내는 self 링크를 찾고, description 설명과 함께 문서화
                                        linkWithRel("item").description("`item` 목록 링크") // 애그리컷 루트로 연결되는 item 링크를 찾고, description 설명과 함께 문서화
                                    )));
    }

    @Test
    void addNewItem() {
        this.webTestClient.post().uri("/hypermedia/items")
                .body(Mono.just(new Item("item-2", "silver candy bar", "실버 캔디바", 30.23)), Item.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();
    }

    @Test
    void findProfile() {
        this.webTestClient.get().uri("/hypermedia/items/profile")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("profile", preprocessResponse(prettyPrint())));
    }


    /** 행동유도성(affordances)이 있는 API test - getItemsWithAffordances */
    @Test
    void findAggregateRootItemAffordances() {
        when(itemService.getItems()).thenReturn(Flux.just(new Item("golf ware", "골프웨어", 240.44)));
        when(itemService.getItem((String) null)).thenReturn(Mono.just(new Item("item-4", "golf ware", "골프웨어", 240.44)));

        this.webTestClient.get().uri("/hypermedia/items/affordances")
                .accept(MediaTypes.HAL_FORMS_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("aggregate-root-affordances", preprocessResponse(prettyPrint())));
    }


    /** 행동유도성(affordances)이 있는 API test - getItemWithAffordances */
    @Test
    void getItemWithAffordances() {
        when(itemService.getItem("item-3")).thenReturn(Mono.just(new Item("item-3", "rainbow bag", "무지개 가방", 332.30)));

        this.webTestClient.get().uri("/hypermedia/items/item-3/affordances")
                .accept(MediaTypes.HAL_FORMS_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("getItem-affordances", preprocessResponse(prettyPrint())));
    }

}
