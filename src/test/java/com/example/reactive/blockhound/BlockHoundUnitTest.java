package com.example.reactive.blockhound;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

public class BlockHoundUnitTest {

    @Test
    void threadSleepIsABlockingCall() {
        // 블록하운드는 리액터 스레드 안에서 사용되는 블로킹 코드를 검출할 수 있음!
        // Mono.delay()를 이용해 전체 플로우를 리액터 스레드에서 실행되게 만든다.
        Mono.delay(Duration.ofSeconds(1))
                .flatMap(tick -> {
                    try {
                        Thread.sleep(10); // 현재 스레드를 멈추게 하는 블로킹 호출
                        return Mono.just(true);
                    } catch (InterruptedException e) {
                        return Mono.error(e);
                    }
                })
                .as(StepVerifier::create)
                //.verifyComplete(); // verifyComplete() 테스트 실패 => java.lang.AssertionError: expectation "expectComplete" failed
                .verifyErrorMatches(throwable -> { // Thread.sleep() 블로킹 호출이 있기 때문에 verifyErrorMatches()로 테스트 통과
                    assertThat(throwable.getMessage())
                            .contains("Blocking call! java.lang.Thread.sleep");
                    return true;
                });
    }
}
