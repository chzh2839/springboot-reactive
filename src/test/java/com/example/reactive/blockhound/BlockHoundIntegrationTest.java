package com.example.reactive.blockhound;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.CartItem;
import com.example.reactive.domain.Item;
import com.example.reactive.repository.CartRepository;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.service.AltItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class BlockHoundIntegrationTest {
    AltItemService itemService;

    @MockBean
    ItemRepository itemRepository;
    @MockBean
    CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        Item sampleItem = new Item("item1", "laptop", "최신형 노트북", 2000);
        CartItem sampleCartItem = new CartItem(sampleItem);
        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

        when(cartRepository.findById(anyString()).thenReturn(Mono.<Cart> empty().hide())); // 비어있는 결과를 리액터로부터 감춘다.
        // Mono.empty() : MonoEmpty 클래스의 싱글턴 객체를 반환.
        // => 테스트 관점에서 블로킹 호출이 알아서 제거되는 문제를 해결하려면 MonoEmpty를 숨겨서 리액터의 최적화 루틴에 걸리지 않게 해야 한다.
        // Mono.hide() : 주목적은 진단을 정확하게 수행하기 위해 식별성 기준 최적화를 방지하는 것
        when(itemRepository.findById(anyString()).thenReturn(Mono.just(sampleItem)));
        when(cartRepository.save(any(Cart.class)).thenReturn(Mono.just(sampleCart)));

        itemService = new AltItemService(itemRepository, cartRepository);
    }

    @Test
    void blockHoundShouldTrapBlockingCall() {
        //Item sampleItem = new Item("item1", "laptop", "최신형 노트북", 2000);
        //itemRepository.save(sampleItem);
        Mono.delay(Duration.ofSeconds(1))
                .flatMap(tick -> itemService.addItemToCart("My Cart", "item1"))
                .as(StepVerifier::create)
                .verifyErrorSatisfies(throwable -> {
                    assertThat(throwable).hasMessageContaining("block()/blockFirst()/blockLast() are blocking");
                });
    }
}
