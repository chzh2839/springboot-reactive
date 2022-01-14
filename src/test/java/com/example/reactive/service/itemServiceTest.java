package com.example.reactive.service;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.CartRepository;
import com.example.reactive.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// @ExtendWith : 테스트 핸들러를 지정할 수 있는 JUnit5 api
// SpringExtension : 스프링에 특화된 테스트 기능을 사용할 수 있게 해준다.
@ExtendWith(SpringExtension.class)
public class itemServiceTest {
    ItemService itemService;

    @MockBean private ItemRepository itemRepository;

    @Test
    void insertItemTest() {
        System.out.println("======== insertItemTest 시작 =============");
        Item newItem = new Item("coffee machine", "커피머신", 100);
        itemService.insertItem(newItem).as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getName(), is("coffee machine"));

                    return true;
                }).verifyComplete();
        System.out.println("======== insertItemTest 종료 =============");
    }

}
