package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

@RestController
public class SpringAmqpItemController {
    private static final Logger log = LoggerFactory.getLogger(SpringAmqpItemController.class);

    private final AmqpTemplate amqpTemplate;

    public SpringAmqpItemController(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    /** messaging producer */
    @PostMapping("/items")
    Mono<ResponseEntity<?>> addNewItemUsingSpringAmqp(@RequestBody Mono<Item> item) {
        // AmqpTemplate는 블로킹API를 호출하므로 subscribeOn을 통해 bounded elastic scheduler에서 관리하는 별도의 스레드에서 실행되게 만든다.
        return item.subscribeOn(Schedulers.boundedElastic())
                .flatMap(content -> {
                    return Mono.fromCallable(() -> { // AmqpTemplate 호출을 Callable로 감싸고 Mono.fromCallable()으로 Mono를 생성한다.

                        // "new-items-spring-amqp"라는 라우팅키(routing key)와 함께 "hacking-spring-boot"  exchange로 Item데이터를 전송한다.
                        this.amqpTemplate.convertAndSend("hacking-spring-boot", "new-items-spring-amqp", content);

                        // 새로 생성되어 추가된 Item객체에 대한 URI를 location헤더에 담아 HTTP 201 Created 상태 코드와 함께 반환한다.
                        return ResponseEntity.created(URI.create("/items")).build();
                    });
                });
    }
}
