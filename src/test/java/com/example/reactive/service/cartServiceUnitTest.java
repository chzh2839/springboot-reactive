package com.example.reactive.service;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.CartItem;
import com.example.reactive.domain.Item;
import com.example.reactive.repository.CartRepository;
import com.example.reactive.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

// @ExtendWith : 테스트 핸들러를 지정할 수 있는 JUnit5 api
// SpringExtension : 스프링에 특화된 테스트 기능을 사용할 수 있게 해준다.
@ExtendWith(SpringExtension.class)
public class cartServiceUnitTest {
    CartService cartService;

    @MockBean private ItemRepository itemRepository;
    @MockBean private CartRepository cartRepository;

    // @MockBean을 사용해 아래 내용 대체
//    @BeforeEach
//    void setUpMock() {
//        itemRepository = mock(ItemRepository.class);
//        cartRepository = mock(CartRepository.class);
//    }

    // 테스트 클래스에 있는 모든 메서드보다 가장 먼저 1회 실행되어야 하는 메소드가 필요하면 @BeforeAll을 사용
    @BeforeEach
    void setUpData() {
        // 테스트 데이터 정의
        Item sampleItem = new Item("item1", "coffee machine", 100, "커피머신", null, 0, false);
        CartItem sampleCartItem = new CartItem(sampleItem);
        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

        // 협력자와의 상호작용 정의
        when(cartRepository.findById(anyString())).thenReturn(Mono.empty());
        when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));

        cartService = new CartService(itemRepository, cartRepository);
    }

    @Test
    void addItemToEmptyCartShouldProduceOneCartItem() {
        System.out.println("======== addItemToEmptyCartShouldProduceOneCartItem test 시작 =============");
        setUpData();
        cartService.addToCart("My Cart", "item1")
                .as(StepVerifier::create) // 테스트 기능을 전담하는 리액터 타입 핸들러 생성 (StepVerifier가 구독시작)
                .expectNextMatches(cart -> {
                    assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
                    .containsExactlyInAnyOrder(1);
//                    assertThat(cart.getCartItems()).extracting(CartItem::getItem)
//                    .containsExactly(new Item("item1", "coffee machine", 100.0, "커피머신", null, 0, false));

                    return true; // expectNextMatches 메서드는 boolean을 반환해야 함
                })
                .verifyComplete();
        System.out.println("======== addItemToEmptyCartShouldProduceOneCartItem test 종료 =============");
    }

    @Test
    void alternativeWayToTest() {
        System.out.println("======== alternativeWayToTest test 시작 =============");
        StepVerifier.create(
                cartService.addToCart("My Cart", "item1"))
                .expectNextMatches(cart -> {
                    assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
                            .containsExactlyInAnyOrder(1);

                    assertThat(cart.getCartItems()).extracting(CartItem::getItem)
                            .containsExactly(new Item("item1", "coffee machine", 100.0, "커피머신", null, 0, false));

                    return true;
                })
                .verifyComplete();
        System.out.println("======== alternativeWayToTest test 종료 =============");
    }


}
