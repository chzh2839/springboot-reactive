package com.example.reactive.service;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SpringAmqpItemService {
    private static final Logger log = LoggerFactory.getLogger(SpringAmqpItemService.class);

    private final ItemRepository itemRepository;

    public SpringAmqpItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** rabbitMQ consumer */
    @RabbitListener( // @RabbitListener: 스프링 AMQP 메시지 리스너로 등록되어 메시지를 소비할 수 있다.
            ackMode = "MANUAL",
            bindings = @QueueBinding( // @QueueBinding : 큐를 익스체인지에 바인딩하는 방법 지정
                        value = @Queue, // @Queue : 임의의 지속성 없는 익명 큐 생성. 인자로 큐 이름을 지정해서 특정 큐를 바인딩할 수도 있다.
                                        //          durable, exclusive, autoDelete 같은 속성값 지정 가능.
                        exchange = @Exchange("hacking-spring-boot"), // @Exchange : 이 큐와 연결될 익스체인지 지정.
                        key = "new-item-spring-amqp")) // 라우팅 키 지정
    public Mono<Void> processNewItemsViaSpringAmqp(Item item) {
        // @RabbitListener에 지정한 내용에 맞는 메시지가 들어오면 processNewItemsViaSpringAmqp()메서드 실행.
        log.debug("Consuming => {}", item);
        return this.itemRepository.save(item).then();
        // 반환타입이 리액터 타입인 Mono이므로 then()을 호출해서 저장이 완료될 때까지 기다린다.
        // 스프링 AMQP는 리액터 타입도 처리할 수 있으므로 구독도 스프링 AMQP에게 위임할 수 있다.
    }
}
