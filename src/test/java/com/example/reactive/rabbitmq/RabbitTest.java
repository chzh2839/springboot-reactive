package com.example.reactive.rabbitmq;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers // JUnit5에서 제공하는 annotation과 testcontainer를 테스트에 사용할 수 있게 해준다.
@ContextConfiguration
public class RabbitTest {
    @Container
    static RabbitMQContainer container = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");

    /** 스프링 팀에서 일반적으로 생성자 주입(constructor injection) 방식의 컴포넌트 주입은 권장하지 않지만,
     * 테스트에서는 생명주기가 다르므로 생성자 주입이 아니라 필드 주입(field injection)을 사용해도 괜찮다. */
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemRepository itemRepository;

    /** @DynamicPropertySource :
     *  Java8의 함수형 인터페이스인 Supplier를 사용해서 환경설정 내용을 Environment에 동적으로 추가한다.
     *  container::getContainerIpAddress, container::getAmqpPort를 사용해서 테스트컨테이너에서 실행한 래빗엠큐 브로커의 호스트 이름과 포트 번호를 가져온다.
     *  이렇게 하면 래빗엠큐 연결 세부정보를 테스트컨테이너에서 읽어와서 스프링 AMQP에서 사용할 수 있도록 스프링 부트 환경설정 정보에 저정한다. */
    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", container::getContainerIpAddress);
        registry.add("spring.rabbitmq.port", container::getAmqpPort);
    }


    /** 메시징 브로커에서 하는 일은
     * 1. 새 메시지를 받을 준비를 하고 기다린다.
     * 2. 새 메시지가 들어오면 꺼내서
     * 3. 몽고디비에 저장한다. */
    @Test
    void verifyMessagingThroughAmqp() throws InterruptedException {
        this.webTestClient.post().uri("/items")
                .bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
                .exchange()
                .expectStatus().isCreated()
                .expectBody();

        Thread.sleep(1500L); // 이걸로 테스트에 사용되는 메시지 처리 순서를 맞출 수 있다.

        this.webTestClient.post().uri("/items")
                .bodyValue(new Item("Smurf TV tray", "nothing important", 29.29))
                .exchange()
                .expectStatus().isCreated()
                .expectBody();

        Thread.sleep(2000L);

        this.itemRepository.findAll()
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getName()).isEqualTo("Alf alarm clock");
                    assertThat(item.getDescription()).isEqualTo("nothing important");
                    assertThat(item.getPrice()).isEqualTo(19.99);
                    return true;
                })
                .expectNextMatches(item -> {
                    assertThat(item.getName()).isEqualTo("Smurf TV tray");
                    assertThat(item.getDescription()).isEqualTo("nothing important");
                    assertThat(item.getPrice()).isEqualTo(29.29);
                    return true;
                })
                .verifyComplete();
    }
}
