package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
public class ItemControllerSecurityTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void verifyLoginPageBlocksAccess() {
        this.webTestClient.get().uri("/") //
                .exchange() //
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "ada")
    void verifyLoginPageWorks() {
        this.webTestClient.get().uri("/") //
                .exchange() //
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "alice", roles = { "SOME_OTHER_ROLE" }) // <1> 실패하는 테스트
    void addingInventoryWithoutProperRoleFails() {
        this.webTestClient.post().uri("/item") // <2>
                .contentType(MediaType.APPLICATION_JSON) // <2>
                .bodyValue("{" + // <3>
                        "\"name\": \"iPhone 11\", " + //
                        "\"description\": \"upgrade\", " + //
                        "\"price\": 999.99" + //
                        "}") //
                .exchange() // <3>
                .expectStatus().isForbidden(); // <4>
    }

    @Test
    @WithMockUser(username = "bob", roles = { "INVENTORY" }) // <1> 성공하는 테스트
    void addingInventoryWithProperRoleSucceeds() {
        this.webTestClient //
                .post().uri("/item") //
                .contentType(MediaType.APPLICATION_JSON) // <2>
                .bodyValue("{" + // <3>
                        "\"itemId\": \"item-1\", " + //
                        "\"name\": \"iPhone 11\", " + //
                        "\"description\": \"upgrade\", " + //
                        "\"price\": 999.99" + //
                        "}") //
                .exchange() //
                .expectStatus().isOk(); // <4>

        this.itemRepository.findById("item-1") // <5>
                .as(StepVerifier::create) // <6>
                .expectNextMatches(item -> { // <7>
                    assertThat(item.getDescription()).isEqualTo("upgrade");
                    assertThat(item.getPrice()).isEqualTo(999.99);
                    return true; // <8>
                }) //
                .verifyComplete(); // <9>
    }

    @Test
    @WithMockUser(username = "carol", roles = { "SOME_OTHER_ROLE" })
    void deletingInventoryWithoutProperRoleFails() {
        this.webTestClient.delete().uri("/item/item-1") //
                .exchange() //
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "dan", roles = { "INVENTORY" })
    void deletingInventoryWithProperRoleSucceeds() {
//        String id = this.itemRepository.findById("item-1") //
//                .map(Item::getItemId) //
//                .block();

        this.webTestClient //
                .delete().uri("/item/item-1") //
                .exchange() //
                .expectStatus().isOk();

        this.itemRepository.findById("item-1") //
                .as(StepVerifier::create) //
                .expectNextCount(0) //
                .verifyComplete();
    }
}
