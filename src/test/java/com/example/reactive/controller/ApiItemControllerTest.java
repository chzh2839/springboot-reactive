package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebTestClientConfigurer;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;

@SpringBootTest
@EnableHypermediaSupport(type = HAL)
@AutoConfigureWebTestClient
public class ApiItemControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    HypermediaWebTestClientConfigurer webClientConfigurer;

    @BeforeEach
    void setUp() {
        this.webTestClient = this.webTestClient.mutateWith(webClientConfigurer);
    }

    @Test
    @WithMockUser(username = "alice", roles = { "SOME_OTHER_ROLE" }) // <1> 실패하는 테스트
    void addingInventoryWithoutProperRoleFails() {
        this.webTestClient //
                .post().uri("/api/items/add") // <2>
                .contentType(MediaType.APPLICATION_JSON) //
                .bodyValue("{" + //
                        "\"name\": \"iPhone X\", " + //
                        "\"description\": \"upgrade\", " + //
                        "\"price\": 999.99" + //
                        "}") //
                .exchange() //
                .expectStatus().isForbidden(); // <3>
    }

    @Test
    @WithMockUser(username = "bob", roles = { "INVENTORY" }) // <1> 성공하는 테스트
    void addingInventoryWithProperRoleSucceeds() {
        this.webTestClient //
                .post().uri("/api/items/add") // <2>
                .contentType(MediaType.APPLICATION_JSON) //
                .bodyValue("{" + //
                        "\"name\": \"iPhone X\", " + //
                        "\"description\": \"upgrade\", " + //
                        "\"price\": 999.99" + //
                        "}") //
                .exchange() //
                .expectStatus().isCreated(); // <3>

        this.itemRepository.findByName("iPhone X") // <4>
                .as(StepVerifier::create) //
                .expectNextMatches(item -> { //
                    assertThat(item.getDescription()).isEqualTo("upgrade");
                    assertThat(item.getPrice()).isEqualTo(999.99);
                    return true; //
                }) //
                .verifyComplete(); //
    }

    @Test
    @WithMockUser(username = "alice")
    void navigateToItemWithoutInventoryAuthority() {
        RepresentationModel<?> root = this.webTestClient.get().uri("/api/items") //
                .exchange() //
                .expectBody(RepresentationModel.class) //
                .returnResult().getResponseBody();

        CollectionModel<EntityModel<Item>> items = this.webTestClient.get() //
                .uri(root.getRequiredLink(IanaLinkRelations.ITEM).toUri()) //
                .exchange() //
                .expectBody(new TypeReferences.CollectionModelType<EntityModel<Item>>() {}) //
                .returnResult().getResponseBody();

        assertThat(items.getLinks()).hasSize(1);
        assertThat(items.hasLink(IanaLinkRelations.SELF)).isTrue();

        EntityModel<Item> first = items.getContent().iterator().next();

        EntityModel<Item> item = this.webTestClient.get() //
                .uri(first.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .exchange() //
                .expectBody(new TypeReferences.EntityModelType<Item>() {}) //
                .returnResult().getResponseBody();

        assertThat(item.getLinks()).hasSize(2);
        assertThat(item.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(item.hasLink(IanaLinkRelations.ITEM)).isTrue();
    }

    @Test
    @WithMockUser(username = "alice", roles = { "INVENTORY" })
    void navigateToItemWithInventoryAuthority() {

        // Navigate to the root URI of the API.
        RepresentationModel<?> root = this.webTestClient.get().uri("/api/items") //
                .exchange() //
                .expectBody(RepresentationModel.class) //
                .returnResult().getResponseBody();

        // Drill down to the Item aggregate root.
        CollectionModel<EntityModel<Item>> items = this.webTestClient.get() //
                .uri(root.getRequiredLink(IanaLinkRelations.ITEM).toUri()) //
                .exchange() //
                .expectBody(new TypeReferences.CollectionModelType<EntityModel<Item>>() {}) //
                .returnResult().getResponseBody();

        assertThat(items.getLinks()).hasSize(2);
        assertThat(items.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(items.hasLink("add")).isTrue();

        // Find the first Item...
        EntityModel<Item> first = items.getContent().iterator().next();

        // ...and extract it's single-item entry.
        EntityModel<Item> item = this.webTestClient.get() //
                .uri(first.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .exchange() //
                .expectBody(new TypeReferences.EntityModelType<Item>() {}) //
                .returnResult().getResponseBody();

        assertThat(item.getLinks()).hasSize(3);
        assertThat(item.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(item.hasLink(IanaLinkRelations.ITEM)).isTrue();
        assertThat(item.hasLink("delete")).isTrue();
    }
}
