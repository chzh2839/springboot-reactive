package com.example.reactive.documentation;

import com.example.reactive.controller.ItemController;
import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.*;

@WebFluxTest(controllers = ItemController.class)
@AutoConfigureRestDocs // 스프링 레스트 독 사용에 필요한 내용 자동 설정
public class ApiItemControllerDocumentationTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    ItemService itemService;

    @Test
    void findingAllItems() {
        when(itemService.getItems()).thenReturn(
                Flux.just(new Item("item1", "up-to-date laptop", "최신형 노트북", 2000.10))
        );

        this.webTestClient.get().uri("/item")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("getItems", preprocessResponse(prettyPrint())));
                /**
                 * document() : 스프링 레스트 독 정적 메서드, 문서 생성 기능을 테스트에 추가하는 역할을 한다.
                 * ** target/generated-snippets/getItems 디렉터리에 문서가 생성된다.
                 * */
    }

    @Test
    void postNewItem() {
        when(itemService.insertItem(any())).thenReturn(
                Mono.just(new Item("item2", "rainbow keyboard", "무지개색 키보드", 150.33))
        );

        this.webTestClient.post().uri("/item")
                .bodyValue(new Item("item2", "rainbow keyboard", "무지개색 키보드", 150.33))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("insert-new-item", preprocessResponse(prettyPrint())));
    }

    @Test
    void findOneItem() {
        when(itemService.getItem("item4")).thenReturn(
                Mono.just(new Item("item4", "black silk blouse", "검정 실크 블라우스", 36.98))
        );

        this.webTestClient.get().uri("/item/item4")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("getItem", preprocessResponse(prettyPrint())));
    }

    @Test
    void updateItem() {
        when(itemService.insertItem(any())).thenReturn(
                Mono.just(new Item("item5", "santa dress for kid - before", "키즈 산타옷", 55.55)));

        this.webTestClient.put().uri("/item/item5")
                .bodyValue(new Item("santa dress for adult - after", "성인 산타옷", 66.66))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("update-item", preprocessResponse(prettyPrint())));
    }
}
