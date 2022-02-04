package com.example.reactive.slice;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
public class MongoDbSliceTest {

    @Autowired
    ItemRepository itemRepository;

    @Test
    void itemRepositorySavesItems() {
        Item sampleItem = new Item("TV tray", "티비 트레이", 44.33);

        itemRepository.save(sampleItem)
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getItemId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("TV tray");
                    assertThat(item.getDescription()).isEqualTo("티비 트레이");
                    assertThat(item.getPrice()).isEqualTo(44.33);

                    return true;
                })
                .verifyComplete();
    }
}
